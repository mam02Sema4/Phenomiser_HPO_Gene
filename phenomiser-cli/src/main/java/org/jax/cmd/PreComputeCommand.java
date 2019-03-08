package org.jax.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jax.dichotomy.DichotomousPair;
import org.jax.io.DichotomousPairParser;
import org.jax.io.DiseaseParser;
import org.jax.io.HpoParser;
import org.jax.services.AbstractResources;
import org.jax.services.ComputedResources;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Parameters(commandDescription = "Precompute similarity score distributions")
public class PreComputeCommand extends PhenomiserCommand {

    private static Logger logger = LoggerFactory.getLogger(PreComputeCommand.class);
    @Parameter(names = {"-hpo", "--hpo_path"}, description = "specify the path to hp.obo")
    private String hpoPath;
    @Parameter(names = {"-da", "--disease_annotation"}, description = "specify the path to disease annotation file .hpoa")
    private String diseasePath;
//    @Parameter(names = {"-db", "--diseaseDB"},
//            description = "choose disease database [OMIM,ORPHA]")
//    private String diseaseDB = "OMIM";
    @Parameter(names = {"-cachePath", "--cachePath"}, description = "specify the path to save precomputed data")
    private String cachePath;
    @Parameter(names = {"-numThreads"}, description = "specify the number of threads")
    private Integer numThreads = 4;
    @Parameter(names = {"-sampling", "--sampling-range"},
            description = "range of HPO terms to create similarity distributions for. Max 10",
            arity = 2)
    private List<Integer> sampling = Arrays.asList(1, 10);
    @Parameter(names = {"-debug"}, description = "use debug mode")
    private boolean debug = false;

    @Override
    public void run() {

        HpoParser hpoParser = new HpoParser(hpoPath);
        hpoParser.init();
        HpoDiseaseAnnotationParser diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseasePath, hpoParser.getHpo());
        DiseaseParser diseaseParser = new DiseaseParser(diseaseAnnotationParser, hpoParser.getHpo());
        try {
            diseaseParser.init();
        } catch (PhenolException e) {
            e.printStackTrace();
            System.exit(1);
        }
        logger.trace("1111");
        Properties properties = new Properties();
        properties.setProperty("numThreads", Integer.toString(numThreads));
        if (cachePath != null) {
            properties.setProperty("cachingPath", cachePath);
        }

        if (sampling.get(0) > sampling.get(1)) {
            System.exit(1);
        }


//        properties.setProperty("diseaseDB", diseaseDB);
        properties.setProperty("sampleMin", Integer.toString(sampling.get(0)));
        properties.setProperty("sampleMax", Integer.toString(sampling.get(1)));


        DichotomousPairParser dichotomousPairParser = new DichotomousPairParser(getClass().getClassLoader().getResourceAsStream("dichotomousPair.csv"));

        AbstractResources resources = new ComputedResources(hpoParser, diseaseParser, dichotomousPairParser, properties, debug);
        resources.init();
    }
}
