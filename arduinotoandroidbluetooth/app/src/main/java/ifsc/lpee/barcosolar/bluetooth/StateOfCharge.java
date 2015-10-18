package ifsc.lpee.barcosolar.bluetooth;

import java.util.Calendar;

/**
 * Created by joaoantoniocardoso on 10/4/15.
 */
public class StateOfCharge {

    public static double
            i_new = 0,
            i_old = 0,
            t_new = 0,
            t_old = 0,
            i_average = 0,
            t_total = 0,
            Qi = 0,
            Q = 1, // capacidade total em Ah
            soc_zero = 1,
            soc_min = 0.2,
            soc = 1,//100%
            k = 1,
            systemEnergy = 0,
            dsystemEnergy = 0,
            systemPower = 0,
            t_remain = 0,
    //dados de descargas conhecidas ,20h -> 38Ah e 1.1h -> 27.5Ah
            R1 = 20,
            C1 = 38,
            R2 = 1.1,
            C2 = 27.5;

    public static int NominalVoltage = 24;
//    private static double test_current = 0; // teste do getCurrent();
//    private static double test_time = 0; // teste do getCurrent();
//    private static int test_iterator = 0;
    public static boolean 	stopSOCWorker = false;

    // classe principal, configurada para executar o teste
    public static void main(String[] args) {
        // configura
        soc_zero = .95;
        soc_min = .5;
        k = peukertConstant(C1, R1, C2, R2);
        Q = R1 * Math.pow((C1 / R1), k);

        // relata entrada
//        System.out.println("Nominal Voltage: " + NominalVoltage + " V");
//        System.out.println("Total Battery Capacity: " + "\n" + "          C: " + C1 + " Ah" + ", E: "
//                + (NominalVoltage * C1) + " Wh for " + R1 + " hours" + "\n" + "          C: " + C2 + " Ah" + ", E: "
//                + (NominalVoltage * C2) + " Wh for " + R2 + " hours");
//        System.out.println("Peukert Constant: " + k);
//        System.out.println("Peukert Capacity: " + Q + " Ah");
//        System.out.println("Peukert Power: " + Q * NominalVoltage + " Wh");
//        System.out.println("Initial SOC: " + soc_zero * 100 + " %");
//        System.out.println("Minimum SOC: " + soc_min * 100 + " %");

        // inicia o monitoramento do estado de carga
        SOC();

        // executa cada iteração ao controle do usuário
//        while (!stopSOCWorker){
//
//        }

        // computa dados de saída
        i_average = nthrt(k, Qi / t_total);

        // relata saída
//        System.out.println("Final SOC: " + soc * 100 + " %");
//        System.out.println("Virtual Discharge Current (mean): " + i_average + " A");
//        System.out.println("Total Virtual Runtime: " + t_total + " hours");
//        System.out.println("test iterations: " + test_iterator);


    }

    // enésema raiz de num
    public static double nthrt(double root, double num) {
        return Math.pow(Math.exp(1 / root), Math.log(num));
    }

    public static double peukertConstant(double C1, double R1, double C2, double R2) {
        return (Math.log(R2) - Math.log(R1)) / (Math.log(C1 / R1) - Math.log(C2 / R2));
    }

    //State Of Charge
    public static void SOC() {
        Thread worker = new Thread(new Runnable() {
            public void run() {

                t_old = getTime();//tempo inicial

                while ((!Thread.currentThread().isInterrupted() && !stopSOCWorker) && MainActivity.connected) {
                    t_new = getTime();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        stopSOCWorker = true;
                    }

                    i_new = Math.pow(getCurrent(), k);
                    if (i_new == 0){
                        //carga = 17,958*tensão^4-943,55*tensão^3+18505*tensão^2+160528*tensão+519755
                            //carga = -41,275*tensao²+1113*tensao-7400,7
                                    i_average = 0;
                                    t_total = 0;
                                    soc_zero = -41.2751*Math.pow(fragment_communication.Voltage1,2)+1113*fragment_communication.Voltage1-7400.7;
                                    systemEnergy = NominalVoltage*Q*(soc);
                    }

                    double systemEnergy_old = systemEnergy;

                    // integral por soma trapezoidal
                    Qi += 0.5 * (i_new + i_old) * (t_new - t_old);
                    t_total += (t_new - t_old);

                    soc = soc_zero - Qi / Q;

                    // computa a energia e sua derivada
                    //systemEnergy = NominalVoltage*i_new*(t_new - t_old);
                    systemEnergy = NominalVoltage*Q*(soc);
                    dsystemEnergy = (systemEnergy - systemEnergy_old)/(t_new - t_old);

                    systemPower = NominalVoltage*i_new;
                    // ou alternativamente: systemEnergy = NominalVoltage*((soc_zero - soc_min)*Q - Qi);


                    //simpleMovingAverage(dsystemEnergy_hist, dsystemEnergy, dsystemEnergy_mean);

                    t_remain =  Q*NominalVoltage*(soc_min - soc)/dsystemEnergy;

                    // recicla
                    t_old = t_new;
                    i_old = i_new;

                }

            }

            // função com os dados do teste do acionamento
            private double getTime() {
                Calendar rightNow = Calendar.getInstance();

                int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                int min = rightNow.get(Calendar.MINUTE);
                int sec = rightNow.get(Calendar.SECOND);

                return (hour + min/60 + sec /(60*60));//retorno em horas
            }

            // função com os dados do teste do acionamento
            private double getCurrent() {

                if (soc <= soc_min) {
                    stopSOCWorker = true;
                }

                return fragment_communication.Current2 - fragment_communication.Current1;//A diferença entre as correntes
            }
        });
        worker.start();
    }


//    public static void simpleMovingAverage(double array[], double newValue, double average){
//        //armazeno o ultimo
//        double oldValue = array[array.length-1];
//
//        //rotaciona e soma por bolha
//        for(int i = 0;i<array.length-1;i++){
//            array[i] = array[i+1];
//        }
//        array[0] = newValue;
//        average += (newValue - oldValue)/array.length;
//    }

}