/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.ACTIVITY_COMPLETED;

/**
 *
 * @author ronen
 */
public class RSA {
    private BigInteger private_key;
    private BigInteger public_key;
    private BigInteger n;
    private final int bitlen = 32;
    
    public RSA() throws BadKeyCalculationException{
        Random rn = new Random();
        BigInteger p1 = GenBigPrime(rn); 
        BigInteger p2 = GenBigPrime(rn);
        n = p1.multiply(p2);
        BigInteger fn = p1.subtract(BigInteger.ONE).multiply(p2.subtract(BigInteger.ONE));
        public_key = new BigInteger(32, rn);
        private_key = FindPrivateKey(fn, public_key, rn);
        if(public_key.multiply(private_key).mod(fn).compareTo(BigInteger.ONE)!=0){
            throw new BadKeyCalculationException();
        }
    }
    
    public RSA(BigInteger prk, BigInteger puk, BigInteger n){
        private_key = prk;
        public_key = puk;
        this.n = n;
    }
    
    private BigInteger GenBigPrime(Random rn){
        BigInteger num = BigInteger.probablePrime(bitlen, rn);
        return num;
    }
  
    private BigInteger FindPrivateKey(BigInteger fn, BigInteger e, Random rn){
        BigInteger orig_fn = new BigInteger(fn+"");
        BigInteger fn2 = new BigInteger(fn+"");
        BigInteger rem  = BigInteger.ONE;
        BigInteger res1;
        BigInteger res2;
        while(e.compareTo(BigInteger.ONE)>0)
        {
            res1 = fn.divide(e);
            res2 = rem.multiply(res1);
            res1 = res1.multiply(e);
            res1 = fn.subtract(res1);
            res2 = fn2.subtract(res2);
            if(res1.compareTo(BigInteger.ZERO)<0){
                res1 = res1.mod(orig_fn);
            }
            if(res2.compareTo(BigInteger.ZERO)<0){
                res2 = res2.mod(orig_fn);
            }
            fn = e;
            fn2 = rem;
            e = res1;
            rem = res2;
        }
        return rem;
    }
    
    public byte[] encript(String data, Key chosenKey){
        byte[] enc_data = null;
        byte[] bdata = data.getBytes(Charset.defaultCharset());
        
        if(bdata != null){
            enc_data = new byte[((bitlen * 2)/8) * data.length()];
            int byte_count = 0;
            
            BigInteger key = null;
            if(chosenKey!=null&&chosenKey.equals(Key.private_key)){
                key = private_key;
            }
            else{
                key=public_key;
            }
            
            for(int i=0;i<bdata.length;i++){
                BigInteger utf8Char = null;
                int inc_i = 0;
                // <editor-fold desc="takes all the bytes related to this utf8 char.">
                if( (bdata[i]&0x80) == 0){
                    utf8Char = BigInteger.valueOf(bdata[i]);
                }
                else if((bdata[i]&0xE0) == 0xC0){
                    utf8Char = BigInteger.valueOf(
                        (bdata[i]&0x3F)* 64 + (bdata[i+1]&0x7F)
                    );
                    inc_i++;
                }
                else if((bdata[i]&0xF0) == 0xE0){
                    utf8Char = BigInteger.valueOf(
                        (
                            (bdata[i]&0x1F) * 64 + (bdata[i+1]&0x7f)
                        ) * 64 + (bdata[i+2]&0x7f)
                    );
                    inc_i+=2;
                }
                // </editor-fold>
                
                utf8Char = utf8Char.modPow(key, n);
                byte[] res = utf8Char.toByteArray();
                if(res.length != ((bitlen * 2)/8)){
                    if(res.length > ((bitlen * 2)/8)){
                        int m =0;
                        while(res[m]==0&&res.length-m>((bitlen * 2)/8)) m++;
                        System.arraycopy(res, m, res, 0, (2 * bitlen)/8);
                        res = Arrays.copyOf(res, (bitlen * 2)/8);
                    }
                    else{
                        int added = ((bitlen * 2)/8) - res.length;
                        res = Arrays.copyOf(res, (bitlen * 2)/8);
                        System.arraycopy(res, 0, res, added, (bitlen * 2)/8-added);
                        for(int m=0;m<added;m++){
                            res[m]=0;
                        }
                    }
                }
                System.arraycopy(res, 0, enc_data, byte_count, res.length);
                byte_count += res.length;
                
                i+=inc_i;
            }
        }
      
        return enc_data;
        
    }
    
    public String Decript(byte[] enc_dt, Key chosenKey){
        String s = "";
        
        if(enc_dt != null){
            
            BigInteger key = null;
            if(chosenKey!=null&&chosenKey.equals(Key.private_key)){
                key = private_key;
            }
            else{
                key=public_key;
            }
            
            for(int i=0;i<enc_dt.length; i+=((bitlen * 2)/8)){
                BigInteger enc_utf8 = null;
                byte[] enc = new byte[(bitlen * 2)/8];
                for(int j=0;j<((bitlen * 2)/8);j++){
                    enc[j] = enc_dt[i+j];
                }
                
                if(enc[0]<0&&enc.length==((bitlen * 2)/8)){
                    enc = Arrays.copyOf(enc, ((bitlen * 2)/8)+1);
                    System.arraycopy(enc, 0, enc, 1, enc.length-1);
                    enc[0] = 0;
                }
                
                enc_utf8 = new BigInteger(enc);
                
                enc_utf8 = enc_utf8.modPow(key, n);
               
                s+= (char)enc_utf8.intValue();
                
            }
            
        }
        
        return s;
        
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getPublic_key() {
        return public_key;
    }
    
    class BadKeyCalculationException extends Exception{
    }
    
}
