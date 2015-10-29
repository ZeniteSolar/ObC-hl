package ifsc.lpee.barcosolar.bluetooth;

import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by joaoantoniocardoso on 10/4/15.
 */
public class StateOfCharge {


    public static double //TODO: mudar variaveis para tipos mais otimizados.
            i_new = 0,
            i_old = 0,
            t_new = 0,
            t_old = 0,
            t_total = 0,
            t_zero = 0,
            k = 1,
            soc_zero = 1, // inicialmente 100% de carga
            soc_min = 0.10, // descarrega até 10% de carga
            soc = 1,
            Qi = 0,
            Qi_old = 0,
            Q_total = 1, // capacidade total corrigida em Ah
            remainingSystemEnergy = 0,
            dremainingSystemEnergy = 0,
            remainingSystemEnergy_old = 0,
            t_left = 0;
    //dados de descargas conhecidas ,20h -> 38Ah e 1.1h -> 27.5Ah
    public static float
            R1 = 20f,
            C1 = 38f,
            R2 = 1.1f,
            C2 = 27.5f;

    public static float NominalVoltage = 24.0f;
    public static boolean stopSOCWorker = false;

    //calcula a constante de Peukert
    public static double peukertConstant(double C1, double R1, double C2, double R2) {
        return (Math.log(R2) - Math.log(R1)) / (Math.log(C1 / R1) - Math.log(C2 / R2));
    }

    //State Of Charge
    public static void SOC() {
        //todo
        Thread worker = new Thread(new Runnable() {
            public void run() {
                k = peukertConstant(C1, R1, C2, R2);
                Q_total = 3600 * R1 * Math.pow((C1 / R1), k); //multiplica por 3600 para ter em Amper-segundo = coulomb
                Log.d("SOC", "k: " + String.format("%f", k) + "Q: " + String.format("%f", Q_total));
                //carga = 17,958*tensao^4-943,55*tensao^3+18505*tensao^2+160528*tensao+519755
                //carga = -41,275*tensao²+1113*tensao-7400,7

                //TODO: criar botão, menu ou tela para calcular SOC a partir da tensao de circuito aberto, criar botao para atualizar o soc atual com o retorno deste calculo

                //DONE:25/10/2015: carregar SOC de arquivo salvo anteriormente.
                //soc_zero = 1;//-41.2751*Math.pow(fragment_communication.Voltage1,2)+1113*fragment_communication.Voltage1-7400.7;

                if (!Configurations.restoreSOCConfigs()) {
                    Log.e("SOC", "Error: can't load configs file, maybe it isn't created yet. It is created when this app is destroyed.");
                    soc_zero = 1.0;
                    //TODO: sugerir para o usuario utilizar a ferramenta para calcular o SOC atraves da tensao de circuito aberto, ou editar o arquivo de configuracao manualmente
                }
                if (Double.isNaN(soc) ) {
                    // tentando tratar um possivel erro, mas a comparacao nao funcionou :'(
                    soc_zero = 1.0;
                }

                soc = soc_zero;

                t_zero = getTime(); //tempo inicial
                t_old = t_zero;

                while (!Thread.currentThread().isInterrupted() && !stopSOCWorker && MainActivity.connected) {

                    t_new = getTime();
                    Log.d("SOC", "t_new: " + String.format("%f", t_new) + "\t t_old: " + String.format("%f", t_old));
                    i_new = Math.pow(getCurrent(), k);
                    Log.d("SOC", "i_new: " + String.format("%f", i_new) + "\t i_old: " + String.format("%f", i_old));

                    // integral por soma trapezoidal
                    Qi = limitToBounds(Qi + ((i_new + i_old) * (t_new - t_old) / 2), Qi, Double.MIN_VALUE, Double.MAX_VALUE);
                    Log.d("SOC", "Qi: " + String.format("%f", Qi) + "/" + String.format("%f", Q_total) );

                    t_total = t_new - t_zero;
                    Log.d("SOC", "t_total: " + String.format("%f", t_total));

                    soc = limitToBounds(soc_zero - Qi / Q_total, soc, Double.MIN_VALUE, Double.MAX_VALUE);

                    Log.d("SOC", "SOC: " + String.format("%f", soc * 100) + " %" +"\t soc_zero: " + String.format("%f", soc_zero * 100) + " %");

                    // computa a energia e sua derivada
                    //systemEnergy = NominalVoltage*i_new*(t_new - t_old);
                    remainingSystemEnergy_old = limitToBounds(remainingSystemEnergy,remainingSystemEnergy_old, Double.MIN_VALUE, Double.MAX_VALUE);

                    remainingSystemEnergy = limitToBounds(NominalVoltage*Q_total - NominalVoltage*Qi,remainingSystemEnergy, Double.MIN_VALUE, Double.MAX_VALUE);
                    dremainingSystemEnergy = limitToBounds((remainingSystemEnergy - remainingSystemEnergy_old) / (t_new - t_old), dremainingSystemEnergy, Double.MIN_VALUE, Double.MAX_VALUE);
                    Log.d("SOC", "systemEnergy: " + String.format("%f", remainingSystemEnergy) + " w" + "\t systemEnergy_old: " + String.format("%f", remainingSystemEnergy_old) + " w");
                    Log.d("SOC", "dsystemEnergy: " + String.format("%f", dremainingSystemEnergy) + " w");

                    t_left = limitToBounds( (soc_min * Q_total * NominalVoltage - remainingSystemEnergy) / (3600 * dremainingSystemEnergy), t_left, Double.MIN_VALUE, Double.MAX_VALUE);

                    Log.d("SOC", "Autonomia: " + String.format("%f", t_left) + " h");

                    // recicla
                    t_old = limitToBounds(t_new, t_old, Double.MAX_VALUE, Double.MIN_VALUE);
                    i_old = limitToBounds(i_new, i_old, Double.MAX_VALUE, Double.MIN_VALUE);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
//                        stopSOCWorker = true;
                        break;
                    }
                }

            }

            // retorna o tempo atual em horas
            private double getTime() {
                return (System.nanoTime()/1000000000.); //retorna em segundos
            }

            // retorna a diferenca entre as correntes
            private double getCurrent() {
                return fragment_communication.Current2 - fragment_communication.Current1;
            }

            private double limitToBounds(double data, double otherwise_data, double min, double max){
//                if(Double.isInfinite(data) | Double.isNaN(data) | data<min | data<max) data = otherwise_data;
                return data;
            }
        });
        worker.start();
    }
}
