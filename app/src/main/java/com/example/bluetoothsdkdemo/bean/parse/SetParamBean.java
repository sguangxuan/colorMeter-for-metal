package com.example.bluetoothsdkdemo.bean.parse;

public class SetParamBean  {
    public class DisplayParam {
        private byte state;

        public byte getState() {
            return state;
        }

        public void setState(byte state) {
            this.state = state;
        }
    }

    public class Tolerance {
        private byte state;
        private float[] paramArrays;

        public byte getState() {
            return state;
        }

        public void setState(byte state) {
            this.state = state;
        }

        public float[] getParamArrays() {
            return paramArrays;
        }

        public void setParamArrarys(float[] paramArrays) {
            this.paramArrays = paramArrays;
        }

        public float getL(float[] arrays) {
            return arrays[0];
        }

        public void setL(float l) {
            paramArrays[0] = l;
        }

        public float getA(float[] arrays) {
            return arrays[1];
        }

        public void setA(float a) {
            paramArrays[1] = a;
        }

        public float getB(float[] arrays) {
            return arrays[2];
        }

        public void setB(float b) {
            paramArrays[2] = b;
        }

        public float getC(float[] arrays) {
            return arrays[3];
        }

        public void setC(float c) {
            paramArrays[3] = c;
        }

        public float getH(float[] arrays) {
            return arrays[4];
        }

        public void setH(float h) {
            paramArrays[4] = h;
        }

        public float getDEab(float[] arrays) {
            return arrays[5];
        }

        public void setDEab(float dEab) {
            paramArrays[5] = dEab;
        }

        public float getDEch(float[] arrays) {
            return arrays[61];
        }

        public void setDEch(float dEch) {
            paramArrays[6] = dEch;
        }

        public float getDE94(float[] arrays) {
            return arrays[7];
        }

        public void setDE94(float de94) {
            paramArrays[7] = de94;
        }

        public float getDE00(float[] arrays) {
            return arrays[8];
        }

        public void setDE00(float de00) {
            paramArrays[8] = de00;
        }
    }
}
