package com.hcpda.smr.biz;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

import com.rscja.utility.StringUtility;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2017/7/18.
 */

public class ISO14443AManager {

    private String TAG = "14443";
    private int bIndex;
    private int bCount;

    public byte[] readM1KeyA(Tag tag, int socter, int block, byte[] accessKey) {
        MifareClassic mc = MifareClassic.get(tag);
        try {
            mc.connect();
            boolean auth = mc.authenticateSectorWithKeyA(socter, accessKey);//auth
            if (auth) {
                //the last block of the sector is used for KeyA and KeyB cannot be overwritted
                byte[] response = mc.readBlock(socter * 4 + block);//read
                return response;
            } else {
                mc.close();
                Log.e(TAG, "auth fail");
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }

    }

    //读取ultralight 标签
    public byte[] readUltralight(Tag tag, int socter, int block, byte[] accessKey, byte[] data16bytes) {
        MifareUltralight mifareUltralight = MifareUltralight.get(tag);
        try {
            mifareUltralight.connect();
            return mifareUltralight.readPages(block);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readUltralightTag(Tag tag, int page) {
        byte[] data = new byte[0];
        for (String th : tag.getTechList()) {
            Log.e(TAG, "readUltralightTag: 有++++" + th);
        }
        Log.e(TAG, "readUltralightTag:    tag  s ---" + tag);
        MifareUltralight mifareUltralight = MifareUltralight.get(tag);

        try {
            mifareUltralight.connect();
            byte[] result = mifareUltralight.readPages(4);
            return new String(result, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            return "读取失败";
        } finally {
            if (mifareUltralight != null) {
                try {
                    mifareUltralight.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean writeUltraLight(Tag tag, int block, byte[] accessPsw) {
        MifareUltralight mifareUltralight = MifareUltralight.get(tag);
        try {
            mifareUltralight.connect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public boolean writeM1KeyA(Tag tag, int socter, int block, byte[] accessKey, byte[] data16bytes) {
        MifareClassic mc = MifareClassic.get(tag);
        try {
            mc.connect();
            boolean auth = mc.authenticateSectorWithKeyA(socter, accessKey);//auth
            if (auth) {
                mc.writeBlock(socter * 4 + block, data16bytes);//write
                return true;
            } else {
                mc.close();
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public byte[] readM1KeyB(Tag tag, int socter, int block, byte[] accessKey) throws IOException {
        MifareClassic mc = MifareClassic.get(tag);
        try {
            mc.connect();
            boolean auth = mc.authenticateSectorWithKeyB(socter, accessKey);//auth
            if (auth) {
                //the last block of the sector is used for KeyA and KeyB cannot be overwritted
                byte[] response = mc.readBlock(socter * 4 + block);//read
                if (mc != null) {
                    mc.close();

                }
                return response;
            } else {
                if (mc != null) {

                    mc.close();
                }
                return null;
            }
        } catch (IOException e) {
            try {

                mc.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        } finally {
            if (mc != null) {
                mc.close();
            }
        }

    }

    public boolean writeM1KeyB(Tag tag, int socter, int block, byte[] accessKey, byte[] data16bytes) {
        MifareClassic mc = null;
        try {
            mc = MifareClassic.get(tag);
        } catch (NullPointerException n) {
            return false;
        }
        if (mc == null) {
            return false;
        }
        try {
            mc.connect();
            boolean auth = mc.authenticateSectorWithKeyB(socter, accessKey);//auth
            if (auth) {
                mc.writeBlock(socter * 4 + block, data16bytes);//write
                mc.close();
                return true;
            } else {
                mc.close();
                return false;
            }
        } catch (IOException e) {
            return false;

        }

    }

    public String readMifareUltralight(Tag tag) {
        MifareUltralight mifareUltralight = MifareUltralight.get(tag);
        try {
            mifareUltralight.connect();
            byte[] page1 = mifareUltralight.readPages(4);
            String result = StringUtility.bytes2HexString(page1, page1.length);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mifareUltralight != null) {
                try {
                    mifareUltralight.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }



}
