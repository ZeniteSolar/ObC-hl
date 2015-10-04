package ifsc.lpee.barcosolar.bluetooth;

/**
 * Created by joaoantoniocardoso on 10/4/15.
 */
public class StateOfCharge {

    public static double
            i_new				= 0,
            i_old				= 0,
            t_new				= 0,
            t_old				= 0,
            i_average 			= 0,
            t_total 			= 0,
            Qi 					= 0,
            Q 					= 1, //capacidade total em amperes*hora
            soc_zero			= 1,
            soc					= 1,
            k 					= 1,
            R1 					= 20,
            C1 					= 38,
            R2 					= 1.1,
            C2 					= 27.5;
    public static int		NominalVoltage		= 24;
    private static double	test_current		= 0; //teste do getCurrent();
    private static double	test_time 			= 0; //teste do getCurrent();
    private static int 		test_iterator		= 0;
    public static boolean 	stopSocWorker 		= false;

    // classe principal, configurada para executar o teste
    public static void main(String[] args){
        //configura
        soc_zero = 1;
        k = peukertConstant(C1,R1,C2,R2);
        Q = R1*Math.pow((C1/R1), k);

        //relata entrada
        System.out.println("Nominal Voltage: " + NominalVoltage + " V");
        System.out.println("Total Battery Capacity: "+ "\n" +
                "          C: " + C1 + " Ah" + ", E: " + (NominalVoltage*C1) + " Wh for " + R1 + " hours" + "\n" +
                "          C: " + C2 + " Ah" + ", E: " + (NominalVoltage*C2) + " Wh for " + R2 + " hours");
        System.out.println("Peukert Constant: " + k);
        System.out.println("Peukert Capacity: " + Q + " Ah");
        System.out.println("Peukert Power: " + Q*NominalVoltage + " Wh");
        System.out.println("Initial SOC: " + soc*100 + " %");

        //inicia o monitoramento do estado de carga
        SOC();
        while(!stopSocWorker); // gasta algum tempo

        // computa dados de saída
        i_average = nthrt(k,Qi/t_total);

        //relata saída
        System.out.println("Final SOC: " + soc*100 + " %");
        System.out.println("Virtual Discharge Current (mean): " + i_average + " A");
        System.out.println("Total Virtual Runtime: " + t_total + " hours");
        System.out.print("iterations: " + test_iterator);
    }

    //enésema raiz de num
    public static double nthrt(double root, double num){
        return Math.pow(Math.exp (1/root),Math.log(num));
    }

    public static double peukertConstant(double C1, double R1, double C2, double R2){
        return (Math.log(R2)-Math.log(R1))/(Math.log(C1/R1)-Math.log(C2/R2));
    }

    public static void SOC() {
        Thread socWorker = new Thread(new Runnable() {
            public void run() {

                t_old = getTime();

                while (!Thread.currentThread().isInterrupted() && !stopSocWorker){

                    t_new = getTime();
                    i_new = Math.pow(getCurrent(), k);

                    // integral por soma trapezoidal
                    Qi += 0.5*(i_new + i_old)*(t_new - t_old);
                    t_total += (t_new - t_old);

                    t_old = t_new;
                    i_old = i_new;

                    soc = soc_zero - Qi/Q;

                }

                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    stopSocWorker = true;
                }
            }

            // função com os dados do teste do acionamento
            private double getTime() {
                //System.nanoTime()*1000000000/(60/60);
                //System.currentTimeMillis()*1000/(60*60);
                if (test_iterator==0) test_time=1; //minutos
                else if (test_iterator==9)test_time = 42;
                else test_time = 5*test_iterator;

                test_iterator++;
                return test_time/60; //horas
            }

            // função com os dados do teste do acionamento
            private double getCurrent() {

                if (soc<=0.2){
                    stopSocWorker = true;
                }

                if (test_iterator==0) test_current		= 11.39;
                else if (test_iterator==1) test_current	= 11.37;
                else if (test_iterator==2) test_current	= 28.17;
                else if (test_iterator==3) test_current	= 27.82;
                else if (test_iterator==4) test_current	= 42.5;
                else if (test_iterator==5) test_current	= 41.45;
                else if (test_iterator==6) test_current	= 59.8;
                else if (test_iterator==7) test_current	= 54.1;
                else if (test_iterator==8) test_current	= 42.77;
                else if (test_iterator==9){
                    test_current			= 39.33;

                }

                return test_current;
            }
        });
        socWorker.start();
    }

}

