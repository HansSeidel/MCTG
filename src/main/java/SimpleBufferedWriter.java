import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class SimpleBufferedWriter extends BufferedWriter {
    public SimpleBufferedWriter(Writer out) {
        super(out);
    }

    /**
     *
     * Each argument passed will be separated by a new line. This method automatically flushes the writer after finished.
     * After all lines are printed, a new line will be added.
     * @param str ...String for each line. If no flush shell be executed write Boolean: false as first parameter
     * @throws IOException
     */
    public void write(String ... str) throws IOException {
        for (String line:str) {
            super.write(line);
            super.newLine();
        }

        super.flush();
    }

    /**
     *
     * Each argument passed will be separated by a new line. This method automatically flushes the writer after finished.
     * After all lines are printed, a new line will be added.
     * @param str ...String for each line. If no flush shell be executed write Boolean: false as first parameter
     * @throws IOException
     */
    public void write(boolean flush, String ... str) throws IOException {
        if(!flush){
            for (String line:str) {
                super.write(line);
                super.newLine();
            }
        }
        write(str);
    }
}
