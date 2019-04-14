import java.io.FileInputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;
import java.util.HashMap;

public class Main
{
    private static class MessageDigestReader
    {
        private MessageDigest messageDigest;
        private Base64.Encoder encoder;
        private byte[] buffer;

        public MessageDigestReader (final int bufferSize)
                throws NoSuchAlgorithmException
        {
            this.messageDigest = MessageDigest.getInstance ("SHA-256");
            this.encoder = Base64.getEncoder ();
            this.buffer = new byte[bufferSize];
        }

        public synchronized String calculate (final Path filePath)
        {
            try {
                final FileInputStream is = new FileInputStream (filePath.toFile ());
                int available;

                this.messageDigest.reset ();

                while ((available = is.available ()) > 0) {
                    int bytesRead = is.read (this.buffer);

                    this.messageDigest.update (this.buffer, 0, bytesRead);
                }

                return this.encoder.encodeToString (this.messageDigest.digest ());
            } catch (final IOException x) {
                x.printStackTrace ();
                return null;
            }
        }
    }

    public static void main (String[] args)
    {
        try {
            final HashMap<String, String> fileToHash = new HashMap<> ();
            final MessageDigestReader digestReader = new MessageDigestReader (1024 * 1024 * 5);

            for (int i = 0; i < args.length; ++i) {
                Files.walk (Paths.get (args[i]))
                        .filter (Files::isRegularFile)
                        .forEach (f -> fileToHash.put (f.toString (), digestReader.calculate (f)));
            }

            System.out.println (fileToHash.size ());
        } catch (final Exception x) {
            x.printStackTrace ();
        }
    }
}
