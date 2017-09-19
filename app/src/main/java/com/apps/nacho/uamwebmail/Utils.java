package com.apps.nacho.uamwebmail;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

/**
 * Created by nacho on 29/12/2016.
 */

public class Utils {

    public Utils() {

    }

    public String[] getTranslatedAddress(String address) throws DecoderException, UnsupportedEncodingException {
        QuotedPrintableCodec qpc = new QuotedPrintableCodec();
        Base64 base64 = new Base64();

        String parts0 = "";
        String parts1 = "";

        if (address.contains("=?utf-8?Q?") || address.contains("=?UTF-8?Q?")) {
            String[] fromParts;
            if (address.contains("=?utf-8?Q?")) {
                fromParts = address.split(Pattern.quote("=?utf-8?Q?"));
            } else {
                fromParts = address.split(Pattern.quote("=?UTF-8?Q?"));
            }
            for (int j = 0; j < fromParts.length; j++) {
                String str = fromParts[j];
                str = str.replace("?= ", "");
                str = qpc.decode(str);
                str = str.replace("_", " ");
                fromParts[j] = str;
            }
            String fromDecoded = "";
            for (String str : fromParts) {
                fromDecoded += str;
            }
            fromParts = fromDecoded.split(Pattern.quote("<"));
            //fromParts[1] = "<" + fromParts[1];
            fromParts[1] = fromParts[1].split(Pattern.quote(">"))[0];
            parts0 = fromParts[0];
            parts1 = (fromParts[1] == null) ? "" : fromParts[1];
        } else if (address.contains("=?utf-8?B?") || address.contains("=?UTF-8?B?") || address.contains("=?UTF-8?b?") || address.contains("=?utf-8?b?")) {
            String[] fromParts;
            fromParts = address.split(" ");
            for (int i = 0; i < fromParts.length; i++) {
                if (fromParts[i].toLowerCase().contains("=?utf-8?b?")) {
                    fromParts[i] = fromParts[i].substring(10);
                    fromParts[i] = new String(base64.decode(fromParts[i].getBytes()));
//                } else if (address.contains("=?UTF-8?B?")) {
//                    fromParts = address.split(Pattern.quote("=?UTF-8?B?"));
//                } else if (address.contains(("=?UTF-8?b?"))) {
//                    fromParts = address.split(Pattern.quote("=?UTF-8?b?"));
//                } else {
//                    fromParts = address.split(Pattern.quote("=?utf-8?b?"));
//                }
                }
                if (!fromParts[i].contains("@")) {
                    parts0 += fromParts[i] + " ";
                } else {
                    parts1 = fromParts[i];
                }
            }
            //address = address.substring(10);
            //fromParts = address.split(Pattern.quote("?= "));
//            String firstPart = "";
//            for (int k = 0; k < fromParts.length; k++) {
//                String str = fromParts[k];
//                if (str.contains("?="))
//                    if (!str.contains("@"))
//                        System.out.println(str);
//                str = new String(base64.decode(str.getBytes()));
//                fromParts[k] = str;
//            }
//            parts0 = fromParts[0];
//            parts1 = (fromParts[1] == null) ? "" : fromParts[1];
        } else if (address.contains("=?iso-8859-1?Q?") || address.contains("=?ISO-8859-1?Q?") ||
                address.contains("=?iso-8859-1?B?") || address.contains("=?ISO-8859-1?B?") || address.contains("=?iso-8859-1?b?")) {
            String[] fromParts;
            if (address.substring(0, 1).equals("\""))
                address = address.substring(1);
            if (address.contains("=?\""))
                address = address.replace("=?\"", "=)");
            if (address.contains("=?iso-8859-1?Q?")) {
                fromParts = address.split(Pattern.quote("=?iso-8859-1?Q?"));
            } else if (address.contains("=?ISO-8859-1?Q?")) {
                fromParts = address.split(Pattern.quote("=?ISO-8859-1?Q?"));
            } else if (address.contains("=?iso-8859-1?B?")) {
                fromParts = address.split(Pattern.quote("=?iso-8859-1?B?"));
            } else if (address.contains("=?ISO-8859-1?B?")) {
                fromParts = address.split(Pattern.quote("=?ISO-8859-1?B?"));
            } else {
                fromParts = address.split(Pattern.quote("=?iso-8859-1?b?"));
            }

            for (int l = 0; l < fromParts.length; l++) {
                fromParts[l] = MimeUtility.decodeText(address);
            }
            String fromDecoded = "";
            for (String str : fromParts)
                fromDecoded += str;
            fromParts = fromDecoded.split(Pattern.quote("<"));
            //fromParts[1] = "<" + fromParts[1];
            fromParts[1] = fromParts[1].split(Pattern.quote(">"))[0];
            parts0 = fromParts[0];
            parts1 = (fromParts[1] == null) ? "" : fromParts[1];
        } else {
            if (address.substring(0, 1).equals("<") || !address.split(Pattern.quote("@"))[0].contains(" ")) {
                if (!address.split(Pattern.quote("@"))[0].contains(" "))
                    address = "<" + address + ">";
                String[] fromParts = {"", address};
                parts0 = fromParts[0];
                parts1 = (fromParts[1] == null) ? "" : fromParts[1];
            } else {
                String[] fromParts = address.split(Pattern.quote(" <"));
                //fromParts[1] = "<" + fromParts[1];
                fromParts[1] = fromParts[1].split(Pattern.quote(">"))[0];
                parts0 = fromParts[0];
                parts1 = (fromParts[1] == null) ? "" : fromParts[1];
            }
        }

        if (parts1.charAt(0) == '<') {
            parts1 = parts1.substring(1);
        }
        if (parts1.charAt(parts1.length() - 1) == '>') {
            parts1 = parts1.substring(0, parts1.length() - 1);
        }
        return new String[]{parts0, parts1};
    }

    public String decodeString(String encoded) throws DecoderException, UnsupportedEncodingException {

        String trimmed = "";
        String decoded = "";

        if (encoded.toLowerCase().contains("=?utf-8?q?")) {
            Log.v("DECODESTRING","utf-8q");
            QuotedPrintableCodec qpc = new QuotedPrintableCodec();
            trimmed = encoded.substring(10);
            trimmed = trimmed.replace("?=", "");
            decoded = qpc.decode(trimmed);
        } else if (encoded.toLowerCase().contains("=?utf-8?b?")) {
            Base64 base64 = new Base64();
            Log.v("DECODESTRING","utf-8?b");
            trimmed = encoded.substring(10);
            trimmed = trimmed.replace("?=", "");
            decoded = new String(base64.decode(trimmed.getBytes()));
        } else if (encoded.toLowerCase().contains("=?iso-8859-1?q?" ) || encoded.toLowerCase().contains("=?iso-8859-1?b?")) {
            Log.v("DECODESTRING","iso-8859-1");
            try {
                decoded = MimeUtility.decodeWord(encoded);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Log.v("DECODESTRING","plain");
            decoded = encoded;
        }

        return decoded;
    }
}
