package bimsit;

import com.bushidowallet.core.bitcoin.bip32.ECKey;
import com.bushidowallet.core.bitcoin.bip32.Seed;
import com.bushidowallet.core.crypto.util.ByteUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.net.URL;
import java.net.URLConnection;


public class Raspberry
{
    // 50000 for mac
    final static int loops = 1;
    final static int hashloops = 100;
    public static void raspberryOverhead()
    {
        Security.addProvider(new BouncyCastleProvider());

        try{

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] teststr = "tx-1mesfgaejfalksdjfalskdfwje".getBytes(StandardCharsets.UTF_8);
            long digestAccumulate = 0;
            long digeststarter = 0;
            long digestender = 0;
            byte[] digest = null;
            for(int i = 0; i < hashloops; i ++) {
                digeststarter = System.nanoTime();
                md.update(teststr);
                digest = md.digest();
                digestender = System.nanoTime();
                digestAccumulate = digestAccumulate + digestender - digeststarter;
                System.out.println( digestender - digeststarter);
            }

            System.out.println(digest.length);
            System.out.println(ByteUtil.toHex(digest));


            System.out.println(digestAccumulate/hashloops);

            byte[] rndseed = SecureRandom.getSeed(32);
            Mac mac = Mac.getInstance ("HmacSHA512", "BC");
            SecretKey seedkey = new SecretKeySpec(Seed.BITCOIN_SEED.getBytes(), "HmacSHA512");
            mac.init (seedkey);
            byte[] lr = mac.doFinal (rndseed);
            byte[] l = Arrays.copyOfRange(lr, 0, 32);
            ECKey ec = new ECKey(l, true);

            long sigAccumulate = 0;
            byte[] signature = null;
            long sigstarter = 0;
            long sigender = 0;
            for(int i = 0; i < loops; i ++) {
                sigstarter = System.nanoTime();
                signature = ec.sign(digest);
                sigender = System.nanoTime();
                sigAccumulate = sigAccumulate + sigender - sigstarter;
            }

            System.out.println(sigAccumulate/loops);

            long verAccumulate = 0;
            long verifystarter =0;
            long verifyender = 0;
            boolean verifed = false;
            for(int i = 0; i < loops; i ++) {
                verifystarter = System.nanoTime();
                verifed = ec.verify(digest,signature);
                verifyender = System.nanoTime();
                verAccumulate = verAccumulate + verifyender - verifystarter;
            }


            System.out.println(verAccumulate/loops);
            System.out.println(verifed);

        } catch (Exception e) {
            e.printStackTrace();
        }


        long requeststarter = 0;
        long requestender = 0;
        requeststarter = System.nanoTime();

        try{
            URL url = new URL("https://api.blockcypher.com/v1/btc/test3/txs/473986c4a3e28166f7751ca9d5a90f88b50a9b8055e58824bc3a61274e096b16?limit=50&includeHex=true");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                for (String line; (line = reader.readLine()) != null;) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        requestender = System.nanoTime();

        System.out.println(requestender-requeststarter);


    }
}
