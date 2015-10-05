package ifsc.lpee.barcosolar.bluetooth;

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
            soc = 1,
            k = 1,
            R1 = 20,
            C1 = 38,
            R2 = 1.1,
            C2 = 27.5,
            systemEnergy = 0,
            dsystemEnergy = 0,
            systemPower = 0,
            t_remain = 0;
    public static int NominalVoltage = 24;
    private static double test_current = 0; // teste do getCurrent();
    private static double test_time = 0; // teste do getCurrent();
    private static int test_iterator = 0;
    public static boolean 	stopSOCWorker = false;

    // classe principal, configurada para executar o teste
    public static void main(String[] args) {
        // configura
        soc_zero = .95;
        soc_min = .5;
        k = peukertConstant(C1, R1, C2, R2);
        Q = R1 * Math.pow((C1 / R1), k);

        // relata entrada
        System.out.println("Nominal Voltage: " + NominalVoltage + " V");
        System.out.println("Total Battery Capacity: " + "\n" + "          C: " + C1 + " Ah" + ", E: "
                + (NominalVoltage * C1) + " Wh for " + R1 + " hours" + "\n" + "          C: " + C2 + " Ah" + ", E: "
                + (NominalVoltage * C2) + " Wh for " + R2 + " hours");
        System.out.println("Peukert Constant: " + k);
        System.out.println("Peukert Capacity: " + Q + " Ah");
        System.out.println("Peukert Power: " + Q * NominalVoltage + " Wh");
        System.out.println("Initial SOC: " + soc_zero * 100 + " %");
        System.out.println("Minimum SOC: " + soc_min * 100 + " %");

        // inicia o monitoramento do estado de carga
        SOC();

        // executa cada iteração ao controle do usuário
        while (!stopSOCWorker){

        }

        // computa dados de saída
        i_average = nthrt(k, Qi / t_total);

        // relata saída
        System.out.println("Final SOC: " + soc * 100 + " %");
        System.out.println("Virtual Discharge Current (mean): " + i_average + " A");
        System.out.println("Total Virtual Runtime: " + t_total + " hours");
        System.out.println("test iterations: " + test_iterator);
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

                t_old = getTime();

                while (!Thread.currentThread().isInterrupted() && !stopSOCWorker) {

                    t_new = getTime();
                    i_new = Math.pow(getCurrent(), k);

                    double systemEnergy_old = systemEnergy;

                    // integral por soma trapezoidal
                    Qi += 0.5 * (i_new + i_old) * (t_new - t_old);
                    t_total += (t_new - t_old);

                    soc = soc_zero - Qi / Q;

                    // computa a energia e sua derivada
                    //systemEnergy = NominalVoltage*i_new*(t_new - t_old);
                    systemEnergy = NominalVoltage*Q*(soc);

                    systemPower = NominalVoltage*i_new;
                    // ou alternativamente: systemEnergy = NominalVoltage*((soc_zero - soc_min)*Q - Qi);

                    dsystemEnergy = (systemEnergy - systemEnergy_old)/(t_new - t_old);

                    //simpleMovingAverage(dsystemEnergy_hist, dsystemEnergy, dsystemEnergy_mean);

                    t_remain =  Q*NominalVoltage*(soc_min - soc)/dsystemEnergy;

                    // recicla
                    t_old = t_new;
                    i_old = i_new;

                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    stopSOCWorker = true;
                }
            }

            // função com os dados do teste do acionamento
            private double getTime() {
                // System.nanoTime()*1000000000/(60/60);
                // System.currentTimeMillis()*1000/(60*60);
                if (test_iterator == 0)
                    test_time = 1; // minutos
                else if (test_iterator == 9)
                    test_time = 42;
                else
                    test_time = 5* test_iterator;

                System.out.println("iteration: " + test_iterator + "; " + "Consuming " + -dsystemEnergy + " W; remaining " + t_remain + " hours to " + soc_min*100 + "% of remaining energy( " + systemEnergy + " Wh ).");
                test_iterator++;
                return test_time / 60; // horas
            }

            // função com os dados do teste do acionamento
            private double getCurrent() {

                if (soc <= soc_min) {
                    stopSOCWorker = true;
                }

                test_current = 1.9;
                if (test_iterator == 0)
                    test_current = 11.39;
                else if (test_iterator == 1)
                    test_current = 11.37;
                else if (test_iterator == 2)
                    test_current = 28.17;
                else if (test_iterator == 3)
                    test_current = 27.82;
                else if (test_iterator == 4)
                    test_current = 42.5;
                else if (test_iterator == 5)
                    test_current = 41.45;
                else if (test_iterator == 6)
                    test_current = 59.8;
                else if (test_iterator == 7)
                    test_current = 54.1;
                else if (test_iterator == 8)
                    test_current = 42.77;
                else if (test_iterator == 9) {
                    test_current = 39.33;

                }

                return test_current;
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