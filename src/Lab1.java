import TSim.*;

import java.awt.*;
import java.util.concurrent.Semaphore;

public class Lab1 {

    private Semaphore upperATrack = new Semaphore(1, true);
    private Semaphore crossing = new Semaphore(1, true);
    private Semaphore aToMiddle = new Semaphore(1, true);
    private Semaphore upperMiddleTrack = new Semaphore(1, true);
    private Semaphore middleToBTrack = new Semaphore(1, true);
    private Semaphore upperBTrack = new Semaphore(1, true);

    private Point aSwitch = new Point(17, 7);
    private Point rightMiddleSwitch = new Point(15, 9);
    private Point leftMiddleSwitch = new Point(4, 9);
    private Point bSwitch = new Point(3, 11);

    private enum Direction {
        A_B, B_A
    }

    private TSimInterface tsi = TSimInterface.getInstance();
    private AddingArrayList<Point> sensors = new AddingArrayList<>();

    public Lab1(Integer speed1, Integer speed2) {
        //Sensors are read up->down, left->right except the station sensors which are meant to be read up->down after the others have been read
        sensors.set(0, new Point(8, 6));
        sensors.set(1, new Point(7, 7));
        sensors.set(2, new Point(10, 7)); //Needed for 15 brake distance?
        sensors.set(3, new Point(15, 7)); //Needed for 15 brake distance?
        sensors.set(4, new Point(18, 7));
        sensors.set(5, new Point(9, 8));
        sensors.set(6, new Point(15, 8)); //Changed because train didn't stop in time
        sensors.set(7, new Point(3, 9));
        sensors.set(8, new Point(6, 9)); //Changed because train didn't stop in time
        sensors.set(9, new Point(13, 9)); //Changed because train didn't stop in time
        sensors.set(10, new Point(16, 9));
        sensors.set(11, new Point(6, 10)); //Changed because train didn't stop in time
        sensors.set(12, new Point(13, 10)); //Changed because train didn't stop in time
        sensors.set(13, new Point(2, 11));
        sensors.set(14, new Point(5, 11)); //Needed for 15 brake distance?
        sensors.set(15, new Point(3, 13)); //Needed for 15 brake distance?
        sensors.set(16, new Point(15, 3)); //Added afterwards, station sensors
        sensors.set(17, new Point(15, 5)); //Added afterwards, station sensors
        sensors.set(18, new Point(15, 11)); //Added afterwards, station sensors
        sensors.set(19, new Point(15, 13)); //Added afterwards, station sensors

        new Train(1, speed1).start();
        new Train(2, speed2).start();
    }

    private class Train extends Thread {

        private int id;
        private int speed;
        Direction direction;
        long stationDelay = 2000;

        public Train(int id, int speed) {
            this.id = id;
            this.speed = speed;
            tsi.setDebug(false);
        }

        @Override
        public void run() {
            super.run();
            try {
                if (id == 1) {
                    direction = Direction.A_B;
                    upperATrack.acquire();
                } else {
                    direction = Direction.B_A;
                    upperBTrack.acquire();
                }
                tsi.setSpeed(id, speed);

                SensorEvent sensorEvent;
                while (true) {

                    sensorEvent = tsi.getSensor(id);

                    Point sensor = new Point(sensorEvent.getXpos(), sensorEvent.getYpos());
                    int i;
                    for (i = 0; i < 20; i++) {
                        if (sensor.equals(sensors.get(i))) {
                            chooseRoute(i, sensorEvent.getStatus());
                            break;
                        }
                    }
                }
            } catch (CommandException | InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        private void chooseRoute(int sensor, int status) throws InterruptedException, TSim.CommandException {
            switch (sensor) {
                case 0:
                case 1:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(true, false, crossing);
                    } else if (status == SensorEvent.INACTIVE) {
                        releaseAction(false,true,crossing);
                    }
                    break;
                case 2:
                case 5:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(false, true, crossing);
                    } else if (status == SensorEvent.INACTIVE) {
                        releaseAction(true, false, crossing);
                    }
                    break;
                case 3:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(true, false, aToMiddle, true, aSwitch, TSimInterface.SWITCH_RIGHT);
                        releaseAction(false, true, aToMiddle);
                    } else if (status == SensorEvent.INACTIVE) {
                        releaseAction(true, false, upperATrack);
                    }
                    break;
                case 4:
                    if (status == SensorEvent.ACTIVE) {
                        chooseAction(false, true, upperATrack, aSwitch, TSimInterface.SWITCH_RIGHT);
                    }
                    break;
                //case 5 is in the second case statement
                case 6:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(true, false, aToMiddle, true, aSwitch, TSimInterface.SWITCH_LEFT);
                        releaseAction(false, true, aToMiddle);
                    }
                    break;
                case 7:
                    if (status == SensorEvent.ACTIVE) {
                        chooseAction(false, true, upperMiddleTrack, leftMiddleSwitch, TSimInterface.SWITCH_LEFT);
                    }
                    break;
                case 8:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(true, false, middleToBTrack, true, leftMiddleSwitch, TSimInterface.SWITCH_LEFT);
                        releaseAction(false, true, middleToBTrack);
                    } else if (status == SensorEvent.INACTIVE) {
                        releaseAction(true, false, upperMiddleTrack);
                    }
                    break;
                case 9:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(false, true, aToMiddle, true, rightMiddleSwitch, TSimInterface.SWITCH_RIGHT);
                        releaseAction(true, false, aToMiddle);
                    } else if (status == SensorEvent.INACTIVE) {
                        releaseAction(false, true, upperMiddleTrack);
                    }
                    break;
                case 10:
                    if (status == SensorEvent.ACTIVE) {
                        chooseAction(true, false, upperMiddleTrack, rightMiddleSwitch, TSimInterface.SWITCH_RIGHT);
                    }
                    break;

                case 11:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(true, false, middleToBTrack, true, leftMiddleSwitch, TSimInterface.SWITCH_RIGHT);
                        releaseAction(false, true, middleToBTrack);
                    }
                    break;

                case 12:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(false, true, aToMiddle, true, rightMiddleSwitch, TSimInterface.SWITCH_LEFT);
                        releaseAction(true, false, aToMiddle);
                    }
                    break;
                case 13:
                    if (status == SensorEvent.ACTIVE) {
                        chooseAction(true, false, upperBTrack, bSwitch, TSimInterface.SWITCH_LEFT);
                    }
                    break;
                case 14:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(false, true, middleToBTrack, true, bSwitch, TSimInterface.SWITCH_LEFT);
                        releaseAction(true,false,middleToBTrack);
                    } else if (status == SensorEvent.INACTIVE) {
                        releaseAction(false, true, upperBTrack);
                    }
                    break;
                case 15:
                    if (status == SensorEvent.ACTIVE) {
                        brakeAction(false, true, middleToBTrack, true, bSwitch, TSimInterface.SWITCH_RIGHT);
                        releaseAction(true,false,middleToBTrack);
                    }
                    break;
                case 16:
                case 17:
                    if (status == SensorEvent.ACTIVE) {
                        stationAction(false,true);
                    }
                    break;
                case 18:
                case 19:
                    if (status == SensorEvent.ACTIVE) {
                        stationAction(true,false);
                    }
                    break;
            }
        }
        //Makes the train wait for the critical zone to be free
        private void brakeAction(boolean A_B, boolean B_A, Semaphore zone) throws TSim.CommandException, InterruptedException {
            brakeAction(A_B, B_A, zone, false, null, 0);
        }
        //Make the train wait for the critical zone to be free and sets the switch in the right position
        private void brakeAction(boolean A_B, boolean B_A, Semaphore zone, boolean hasSwitch, Point trackSwitch, int switchDirection) throws TSim.CommandException, InterruptedException {
            if (((direction == Direction.A_B) && A_B) || ((direction == Direction.B_A) && B_A)) {
                tsi.setSpeed(id, 0);
                zone.acquire();
                if (hasSwitch) {
                    tsi.setSwitch(trackSwitch.x, trackSwitch.y, switchDirection);
                }
                tsi.setSpeed(id, speed);
            }
        }
        //Makes the train release the critical zone it just left
        private void releaseAction(boolean A_B, boolean B_A, Semaphore zone) {
            if (((direction == Direction.A_B) && A_B) || ((direction == Direction.B_A) && B_A)) {
                zone.release();
            }
        }
        //Chooses the preferred track at the middle and two stations if it's free, otherwise it selects the other track
        private void chooseAction(boolean A_B, boolean B_A, Semaphore zone, Point trackSwitch, int switchDirection) throws TSim.CommandException {
            if (((direction == Direction.A_B) && A_B) || ((direction == Direction.B_A) && B_A)) {
                if (zone.tryAcquire()) {
                    tsi.setSwitch(trackSwitch.x, trackSwitch.y, switchDirection); //B splitter
                } else {
                    tsi.setSwitch(trackSwitch.x, trackSwitch.y, (switchDirection % 0x02) + 0x01); //B splitter hex magic
                }
            }
        }
        //Stops the train at the station and waits 2 seconds
        private void stationAction(boolean A_B, boolean B_A) throws TSim.CommandException, InterruptedException {
            if (((direction == Direction.A_B) && A_B) || ((direction == Direction.B_A) && B_A)) {
                tsi.setSpeed(id,0);
                sleep(stationDelay);
                changeDirection();
                tsi.setSpeed(id,speed);
            }
        }
        //Changes the direction and sets the speed to the opposite i.e. reverse
        private void changeDirection() {
            speed = speed * -1;
            if (direction == Direction.A_B) {
                direction = Direction.B_A;
            } else if (direction == Direction.B_A) {
                direction = Direction.A_B;
            }
        }
    }
}
