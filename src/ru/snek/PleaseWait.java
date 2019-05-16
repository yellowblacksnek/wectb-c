package ru.snek;

import static ru.snek.Printer.*;

class PleaseWait implements Runnable {
    private volatile boolean stop = false;
    private volatile int percentage = 0;
    private static final String preparing = "Подготовка запроса для отправки на сервер";
    private static final String sending = "Отправка запроса на сервер";
    private static final String waiting = "Ожидание ответа от сервера";
    private static final String receiving = "Получение ответа от сервера";
    private volatile int stage = 0;
    private static final int maxDots = 3;
    private int dots = 0;

    public PleaseWait() {
        new Thread(this).start();
    }
    @Override
    public void run() {
        try {
            //Thread.sleep(200);
            String toPrint = "";
            //int count = 0;
            long lastUpdate = System.currentTimeMillis();
            while(true) {
                if(stop) break;
                int timeToWait = 0;
                if(stage == 0 || stage == 2) timeToWait = 250;
                else timeToWait = 100;
                long current = System.currentTimeMillis();
                long passed = current - lastUpdate;
                if(passed > timeToWait) {
                    toPrint = getPrintString();
                    lastUpdate = current;
                    if(stop) break;
                    clearLine();
                    print(toPrint+'\r');
                }
                Thread.sleep(300);
            }
        }catch (InterruptedException e) { }
    }

    private String nextDots() {
        ++dots;
        if(dots > 3) dots = 0;
        String dotsString = "";
        for(int i = 0; i < dots; ++i)
            dotsString += '.';
        return dotsString;
    }

    private String getPrintString() {
        switch(stage) {
            case 0:
                return preparing + nextDots();
            case 1:
                return sending + ": " + percentage +"%";
            case 2:
                return waiting + nextDots();
            case 3:
                return receiving + ": " + percentage +"%";
            case 4:
                return sending + nextDots();
        }
        return "";
    }

    public void setStage(int s) {
        if(s < 0 || s > 4) return;
        stage = s;
        dots = 0;
        percentage = 0;
    }

    public void setPercentage(int p) {percentage = p;}

    public void stop() {
        stop = true;
        clearLine();
    }
}

//                switch(stage) {
//                    case 0:
//                        long current = System.currentTimeMillis();
//                        long passed = current - lastUpdate;
//                        if(passed > timeToWait) {
//                            toPrint = preparing + nextDots();
//                            lastUpdate = current;
//                        }
//                        break;
//                    case 1:
//                        toPrint = sending + percentage +"%";
//                        break;
//                    case 2:
//                }
//                String add = "";
//                if(count > dots+1) count = 1;
//                for(int i = 0; i < count; ++i)
//                    add += ".";
//                add += '\r';
//                    if (count == 4) {
//                        clearLine();
//                        print(s+'\r');
//                    }
//                    else print(s+add);
//                    ++ count;