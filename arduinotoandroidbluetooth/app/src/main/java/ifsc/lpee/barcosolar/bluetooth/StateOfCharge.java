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
            Qi = 0,
            Q = 1, // capacidade total em Ah
            soc_zero = 1,
            soc_min = 0.5,
            soc = 1,//100%
            k = 1,
            systemEnergy = 0,
            dsystemEnergy = 0,
            t_remain = 0,
    //dados de descargas conhecidas ,20h -> 38Ah e 1.1h -> 27.5Ah
    R1 = 20,
            C1 = 38,
            R2 = 1.1,
            C2 = 27.5;

    public static float NominalVoltage = 12f;
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
                Q = R1 * Math.pow((C1 / R1), k);
                //carga = 17,958*tensao^4-943,55*tensao^3+18505*tensao^2+160528*tensao+519755
                //carga = -41,275*tensao²+1113*tensao-7400,7

                //TODO: criar botão, menu ou tela para calcular SOC a partir da tensao de circuito aberto, criar botao para atualizar o soc atual com o retorno deste calculo

                //DONE:25/10/2015: carregar SOC de arquivo salvo anteriormente.
                //soc_zero = 1;//-41.2751*Math.pow(fragment_communication.Voltage1,2)+1113*fragment_communication.Voltage1-7400.7;
                if (!Configurations.restoreSOCConfigs()) {
                    Log.e("SOC", "Error: can't load configs file, maybe it isn't created yet. It is created when this app is destroyed.");
                    soc_zero = 1;
                    //TODO: sugerir para o usuario utilizar a ferramenta para calcular o SOC atraves da tensao de circuito aberto, ou editar o arquivo de configuracao manualmente
                }
                if (Double.isNaN(soc) ) {
                    // tentando tratar um possivel erro, mas a comparacao nao funcionou :'(
                    soc_zero = 1;
                }

                soc = soc_zero;

                systemEnergy = NominalVoltage * Q * (soc_zero);
                double systemEnergy_old;

                t_old = getTime();//tempo inicial
                while (!Thread.currentThread().isInterrupted() && !stopSOCWorker && MainActivity.connected) {

                    t_new = getTime();
                    Log.d("SOC", "t_new: " + String.format("%f", t_new) + "\t t_old: " + String.format("%f", t_old));
                    i_new = Math.pow(getCurrent(), k);
                    Log.d("SOC", "i_new: " + String.format("%f", i_new) + "\t i_old: " + String.format("%f", i_old));

                    // integral por soma trapezoidal
                    Qi += 0.5 * (i_new + i_old) * (t_new - t_old);
                    Log.d("SOC", "Qi: " + String.format("%f", Qi));

                    t_total += (t_new - t_old);
                    Log.d("SOC", "t_total: " + String.format("%f", t_total));

                    soc = soc_zero - (Qi / Q);
                    Log.d("SOC", "SOC: " + String.format("%f", soc * 100) + " %");

                    // computa a energia e sua derivada
                    systemEnergy_old = systemEnergy;
                    //systemEnergy = NominalVoltage*i_new*(t_new - t_old);
                    systemEnergy = (NominalVoltage) * Q * (soc);
                    dsystemEnergy = (systemEnergy - systemEnergy_old) / (t_new - t_old);
                    Log.d("SOC", "dsystemEnergy: " + String.format("%f", dsystemEnergy) + " w");

                    t_remain = Q * NominalVoltage * (soc_min - soc) / dsystemEnergy;
                    Log.d("SOC", "Autonomia: " + String.format("%f", t_remain) + " h");

                    // recicla
                    t_old = t_new;
                    i_old = i_new;

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
                return (double) (System.nanoTime()/1000000000.)/(60.*60.);
            }

            // retorna a diferenca entre as correntes
            private double getCurrent() {
                return fragment_communication.Current2 - fragment_communication.Current1;
            }
        });
        worker.start();
    }
}
