package com.larsWare;

import com.garmin.fit.*;

import java.io.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {


        Parser parser = new Parser();
        if (args.length == 1) {
            parser.setFilename(args[0]);
            String out  = getOutfile(args[0]);
            parser.setOutput(out);
            //parser.read();
            parser.writeCoordinates();
            System.out.println("writing file " + out);
        } else {
            System.out.println("Usage: larsFitReader FITFILENAME");
        }
    }

    private static String getOutfile(String f) {
        //TODO make this better
        String f2 = f.substring(0, f.length() - 4) + ".csv";
        return f2;
    }

    private static class Parser {
        private String FITfilename;
        FileInputStream inputStream;
        FileOutputStream outStream;
        Listener listener = new Listener();
        final Decode decode = new Decode();
        private String header = "latitude,longitude,speed,heartrate";

        void setFilename(String fitFileName) {
            FITfilename = fitFileName;
            this.checkFITFile();
        }

        void setOutput(String outFile) {
            File of = new File(outFile);
            try {
                if (!of.exists()) {
                    of.createNewFile();
                }
                outStream = new FileOutputStream(outFile);
                header += "\n"; //TODO better way?
                byte[] b = header.getBytes();
                outStream.write(header.getBytes());
            } catch (FileNotFoundException e) {
                System.err.println("File: " + outFile + " cannot be written");
                e.printStackTrace();    // DEBUG
                System.exit(1);
            } catch (IOException e) {
                System.err.println("File creation error: " + outFile);
                        e.printStackTrace();    // DEBUG
                System.exit(1);
            }
        }

        void checkFITFile() {

            try {
                inputStream = new FileInputStream(FITfilename);
            } catch (FileNotFoundException e) {
                System.err.println("File: " + FITfilename + " cannot be found");
                e.printStackTrace();    // DEBUG
                System.exit(1);
            }
        }

        private void initialize() {

        }

        void read() {

            MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
            mesgBroadcaster.addListener((FileIdMesgListener) listener);
            mesgBroadcaster.addListener((UserProfileMesgListener) listener);
            mesgBroadcaster.addListener((DeviceInfoMesgListener) listener);
            mesgBroadcaster.addListener((MonitoringMesgListener) listener);
            mesgBroadcaster.addListener((RecordMesgListener) listener);

            try {
                decode.read(inputStream, mesgBroadcaster, mesgBroadcaster);

            } catch (FitRuntimeException e) {
                System.err.println("Exception decoding file: ");
                System.err.println(e.getMessage());
                return;
            }

            try {
                inputStream.close();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }


        }

        void writeCoordinates() {
            listener.setOutputStream(outStream);
            MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
            mesgBroadcaster.addListener((RecordMesgListener) listener);

            try {
                decode.read(inputStream, mesgBroadcaster, mesgBroadcaster);

            } catch (FitRuntimeException e) {
                System.err.println("Exception decoding file: ");
                System.err.println(e.getMessage());
                return;
            }

            try {
                inputStream.close();
                outStream.close();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    //private static class Listener implements FileIdMesgListener {
    private static class Listener implements FileIdMesgListener, UserProfileMesgListener, DeviceInfoMesgListener, MonitoringMesgListener, RecordMesgListener, DeveloperFieldDescriptionListener {

        private FileOutputStream outStream;
        private boolean writeFile = false;

        public void setOutputStream(FileOutputStream out) {
            writeFile = true;
            outStream = out;
        }

        @Override
        public void onMesg(FileIdMesg mesg) {
            System.out.println("File ID:");

            if (mesg.getType() != null) {
                System.out.print("   Type: ");
                System.out.println(mesg.getType().getValue());
            }

            if (mesg.getManufacturer() != null) {
                System.out.print("   Manufacturer: ");
                System.out.println(mesg.getManufacturer());
            }

            if (mesg.getProduct() != null) {
                System.out.print("   Product: ");
                System.out.println(mesg.getProduct());
            }

            if (mesg.getSerialNumber() != null) {
                System.out.print("   Serial Number: ");
                System.out.println(mesg.getSerialNumber());
            }

            if (mesg.getNumber() != null) {
                System.out.print("   Number: ");
                System.out.println(mesg.getNumber());
            }
        }

        @Override
        public void onMesg(UserProfileMesg mesg) {
            System.out.println("User profile:");

            if (mesg.getFriendlyName() != null) {
                System.out.print("   Friendly Name: ");
                System.out.println(mesg.getFriendlyName());
            }

            if (mesg.getGender() != null) {
                if (mesg.getGender() == Gender.MALE) {
                    System.out.println("   Gender: Male");
                } else if (mesg.getGender() == Gender.FEMALE) {
                    System.out.println("   Gender: Female");
                }
            }

            if (mesg.getAge() != null) {
                System.out.print("   Age [years]: ");
                System.out.println(mesg.getAge());
            }

            if (mesg.getWeight() != null) {
                System.out.print("   Weight [kg]: ");
                System.out.println(mesg.getWeight());
            }
        }

        @Override
        public void onMesg(DeviceInfoMesg mesg) {
            System.out.println("Device info:");

            if (mesg.getTimestamp() != null) {
                System.out.print("   Timestamp: ");
                System.out.println(mesg.getTimestamp());
            }

            if (mesg.getBatteryStatus() != null) {
                System.out.print("   Battery status: ");

                switch (mesg.getBatteryStatus()) {
                    case BatteryStatus.CRITICAL:
                        System.out.println("Critical");
                        break;
                    case BatteryStatus.GOOD:
                        System.out.println("Good");
                        break;
                    case BatteryStatus.LOW:
                        System.out.println("Low");
                        break;
                    case BatteryStatus.NEW:
                        System.out.println("New");
                        break;
                    case BatteryStatus.OK:
                        System.out.println("OK");
                        break;
                    default:
                        System.out.println("Invalid");
                        break;
                }
            }
        }

        @Override
        public void onMesg(MonitoringMesg mesg) {
            System.out.println("Monitoring:");

            if (mesg.getTimestamp() != null) {
                System.out.print("   Timestamp: ");
                System.out.println(mesg.getTimestamp());
            }

            if (mesg.getActivityType() != null) {
                System.out.print("   Activity Type: ");
                System.out.println(mesg.getActivityType());
            }

            // Depending on the ActivityType, there may be Steps, Strokes, or Cycles present in the file
            if (mesg.getSteps() != null) {
                System.out.print("   Steps: ");
                System.out.println(mesg.getSteps());
            } else if (mesg.getStrokes() != null) {
                System.out.print("   Strokes: ");
                System.out.println(mesg.getStrokes());
            } else if (mesg.getCycles() != null) {
                System.out.print("   Cycles: ");
                System.out.println(mesg.getCycles());
            }

            printDeveloperData(mesg);
        }

        @Override
        public void onMesg(RecordMesg mesg) {
            if (writeFile) {
                String s;
                s = calcDegrees(mesg.getPositionLat()) + "," + calcDegrees(mesg.getPositionLong()) + "," + mesg.getSpeed() + "," +
                        mesg.getHeartRate() + "\n"; //TODO what's the correct way to add newline??
                byte[] b = s.getBytes();
                try {
                    outStream.write(b);
                    outStream.flush();
                } catch (IOException e) {
                    System.err.println("Error writing stream");
                    e.printStackTrace();
                    //TODO exit??
                }


            } else {
                System.out.println("Record:");

                printValues(mesg, RecordMesg.HeartRateFieldNum);
                //printValues(mesg, RecordMesg.CadenceFieldNum);
                printValues(mesg, RecordMesg.DistanceFieldNum);
                printValues(mesg, RecordMesg.SpeedFieldNum);
                printValues(mesg, RecordMesg.AltitudeFieldNum);
                printValues(mesg, RecordMesg.PositionLatFieldNum);
                printValues(mesg, RecordMesg.PositionLongFieldNum);

                printDeveloperData(mesg);
            }
        }

        private String calcDegrees(int i) {

            double d = i * (180 / Math.pow(2,31));
            return d +"";   //TODO lazy
        }

        private void printDeveloperData(Mesg mesg) {
            for (DeveloperField field : mesg.getDeveloperFields()) {
                if (field.getNumValues() < 1) {
                    continue;
                }

                if (field.isDefined()) {
                    System.out.print("   " + field.getName());

                    if (field.getUnits() != null) {
                        System.out.print(" [" + field.getUnits() + "]");
                    }

                    System.out.print(": ");
                } else {
                    System.out.print("   Undefined Field: ");
                }

                System.out.print(field.getValue(0));
                for (int i = 1; i < field.getNumValues(); i++) {
                    System.out.print("," + field.getValue(i));
                }

                System.out.println();
            }
        }

        @Override
        public void onDescription(DeveloperFieldDescription desc) {
            System.out.println("New Developer Field Description");
            System.out.println("   App Id: " + desc.getApplicationId());
            System.out.println("   App Version: " + desc.getApplicationVersion());
            System.out.println("   Field Num: " + desc.getFieldDefinitionNumber());
        }

        private void printValues(Mesg mesg, int fieldNum) {
            Iterable<FieldBase> fields = mesg.getOverrideField((short) fieldNum);
            Field profileField = Factory.createField(mesg.getNum(), fieldNum);
            boolean namePrinted = false;

            if (profileField == null) {
                return;
            }

            for (FieldBase field : fields) {
                if (!namePrinted) {
                    System.out.println("   " + profileField.getName() + ":");
                    namePrinted = true;
                }

                if (field instanceof Field) {
                    System.out.println("      native: " + field.getValue());
                } else {
                    System.out.println("      override: " + field.getValue());
                }
            }
        }
    }
}
