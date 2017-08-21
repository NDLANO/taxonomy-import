package no.ndla.taxonomy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import no.ndla.taxonomy.client.TaxonomyRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Iterator;

@SpringBootApplication
public class ImporterApplication {


    @Parameter(names = {"-e", "--endpoint"})
    private static String endpoint = "http://localhost:5000";

    @Parameter(names = {"-i", "--subject-id"}, required = true)
    private static String subjectId;

    @Parameter(names = {"-n", "--subject-name"})
    private static String subjectName;

    @Parameter(names = "--help", help = true)
    private static boolean help;

    public static void main(String[] args) throws Exception {

        ImporterApplication app = new ImporterApplication();

        JCommander jCommander = new JCommander(app);

        try {
            jCommander.parse(args);
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(e.getMessage()).append("\n\n");
            jCommander.usage(stringBuilder);
            System.err.println(stringBuilder.toString());
            return;
        }

        if (app.help) {
            jCommander.usage();
            return;
        }

        ConfigurableApplicationContext context = SpringApplication.run(ImporterApplication.class, args);
        Importer importer = context.getBean(Importer.class);
        app.run(importer);
    }

    private void run(Importer importer) throws Exception {
        TsvParser.StringIterator iterator = new InputStreamStringIterator(System.in);

        Entity subject = new Entity() {{
            id = URI.create(subjectId);
            name = subjectName;
            type = "Subject";
        }};

        importer.doImport(subject);

        Iterator<Entity> entities = new TsvParser(iterator, subject);
        while (entities.hasNext()) {
            Entity entity = entities.next();
            importer.doImport(entity);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public TaxonomyRestClient restClient(RestTemplate restTemplate) {
        return new TaxonomyRestClient(endpoint, restTemplate);
    }

    @Bean
    public Importer importer(TaxonomyRestClient restClient) {
        return new Importer(restClient);
    }

    private static class InputStreamStringIterator extends TsvParser.StringIterator {
        public static final int HEADER_LINES_IN_SPREADSHEET = 2;
        private final BufferedReader reader;

        private int lineNumber = -1;

        public InputStreamStringIterator(InputStream inputStream) throws Exception {
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        }

        @Override
        public boolean hasNext() {
            try {
                return reader.ready();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String next() {
            try {
                lineNumber++;
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getLineNumber() {
            return lineNumber + HEADER_LINES_IN_SPREADSHEET;
        }
    }
}
