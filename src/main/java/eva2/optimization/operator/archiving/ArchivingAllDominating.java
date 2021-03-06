package eva2.optimization.operator.archiving;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.selection.SelectBestIndividuals;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * This simple strategy simply archives all Pareto optimal solutions. This method is
 * very prone to OutOfMemory errors!
 */
@Description("This is a straightforward strategy, which selects all dominating individuals (very prone to generate OutOfMemory errors).")
public class ArchivingAllDominating extends AbstractArchiving implements java.io.Serializable {


    public ArchivingAllDominating() {
    }

    public ArchivingAllDominating(ArchivingAllDominating a) {
        this.obeyDebsConstViolationPrinciple = a.obeyDebsConstViolationPrinciple;
    }

    @Override
    public Object clone() {
        return new ArchivingAllDominating(this);
    }

    /**
     * This method allows you to merge to populations into an archive.
     * This method will add elements from pop to the archive but will also
     * remove elements from the archive if the archive target size is exceeded.
     *
     * @param pop The population that may add Individuals to the archive.
     */
    @Override
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) {
            pop.SetArchive(new Population());
        }
//        System.out.println("addElementsToArchive");
        if (this.obeyDebsConstViolationPrinciple) {
            for (int i = 0; i < pop.size(); i++) {
                //System.out.println("i:"+ i+" "+pop.size()+"_"+((AbstractEAIndividual)pop.get(0)).getFitness().length);
                if ((pop.get(i).getConstraintViolation() == 0) && (this.isDominant(pop.get(i), pop.getArchive()))) {
                    //System.out.println("Adding ("+((AbstractEAIndividual)pop.get(i)).getFitness()[0] +"/"+((AbstractEAIndividual)pop.get(i)).getFitness()[1]+") to archive.");
                    this.addIndividualToArchive((AbstractEAIndividual) pop.get(i).clone(), pop.getArchive());
                }
            }
            if ((pop.getArchive().size() == 0) && (pop.size() > 0)) {
                SelectBestIndividuals select = new SelectBestIndividuals();
                select.setObeyDebsConstViolationPrinciple(true);
                pop.getArchive().addPopulation(select.selectFrom(pop, pop.getArchive().getTargetSize()));
            }
        } else {
            // test for each element in population if it
            // is dominating a element in the archive
            for (int i = 0; i < pop.size(); i++) {
                //System.out.println("i:"+ i+" "+pop.size()+"_"+((AbstractEAIndividual)pop.get(0)).getFitness().length);
                if (this.isDominant(pop.get(i), pop.getArchive())) {
                    //System.out.println("Adding ("+((AbstractEAIndividual)pop.get(i)).getFitness()[0] +"/"+((AbstractEAIndividual)pop.get(i)).getFitness()[1]+") to archive.");
                    this.addIndividualToArchive((AbstractEAIndividual) pop.get(i).clone(), pop.getArchive());
                }
            }
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "AllDominating";
    }

}