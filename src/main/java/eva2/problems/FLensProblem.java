package eva2.problems;


import eva2.gui.JEFrame;
import eva2.gui.StandaloneOptimization;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.math.RNG;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class MyLensViewer extends JPanel implements InterfaceSolutionViewer {

    /**
     *
     */
    private static final long serialVersionUID = 7945150208043416139L;
    Population indiesToPaint = new Population();
    private int theHeight, theWidth;
    FLensProblem lensProblem;

    public MyLensViewer(FLensProblem f) {
        initView(f);
        Dimension d = new Dimension(280, 220);
        this.setPreferredSize(d);
        setIgnoreRepaint(true);
        this.setMinimumSize(d);
        resetView();
    }

    @Override
    public void initView(AbstractOptimizationProblem prob) {
        this.lensProblem = (FLensProblem) prob;
    }

    @Override
    public void resetView() {
        ESIndividualDoubleData dummy = new ESIndividualDoubleData();
        dummy.setFitness(new double[]{Double.POSITIVE_INFINITY});
        indiesToPaint = new Population();
    }

    @Override
    public void paint(Graphics g) {
        Shape tmpShape;
        BufferedImage bufferedImage;
        BasicStroke ds = new BasicStroke();
        Stroke dashStroke;
        int mag = 10;
        int centerLens, centerScreen, segment;

        dashStroke = new BasicStroke(ds.getLineWidth(), ds.getEndCap(), ds.getLineJoin(), ds.getMiterLimit(), new float[]{8, 8}, 0);

        // This might cure the eternal display problems: just ignore clipping and leave it up to swing
        Dimension winDim = getSize();
        theHeight = winDim.height;
        theWidth = winDim.width;

        bufferedImage = new BufferedImage(this.theWidth, this.theHeight, BufferedImage.TYPE_INT_RGB);
        // Create a graphics contents on the buffered image
        Graphics2D g2D = bufferedImage.createGraphics();

        super.paint(g2D);
        g2D.setPaint(Color.white);
        tmpShape = new Rectangle(0, 0, this.theWidth, this.theHeight);
        g2D.fill(tmpShape);

        // now start to plot some interesting stuff
        //draw the mid line
        g2D.setPaint(Color.black);
        g2D.drawLine(0, this.theHeight / 2, this.theWidth, this.theHeight / 2);
        centerLens = 5 + 50;
        centerScreen = centerLens + (int) this.lensProblem.focalLength * 10;
        segment = 10 * (int) this.lensProblem.radius * 2 / (this.lensProblem.problemDimension - 1);
        g2D.setStroke(dashStroke);
        g2D.drawLine(centerLens, this.theHeight / 2 + (int) this.lensProblem.radius * 10, centerLens, this.theHeight / 2 - (int) this.lensProblem.radius * 10);
        g2D.drawLine(centerScreen, this.theHeight / 2 + (int) this.lensProblem.radius * 10 + 10, centerScreen, this.theHeight / 2 - (int) this.lensProblem.radius * 10 - 10);
        g2D.setStroke(ds);
        paintLens(lensProblem.problemDimension, theHeight, lensProblem.radius, mag, centerLens, centerScreen, segment, g2D);
        // Now put everything on the screen
        g.drawImage(bufferedImage, 0, 0, this);
    }

    private void paintLens(int dim, int height, double radius, int mag, int centerLens, int centerScreen, int segment, Graphics2D g2D) {
        for (int i = 0; i < indiesToPaint.size(); i++) {
            AbstractEAIndividual indy = indiesToPaint.getEAIndividual(i);
            paintLens(indy.getDoublePosition(), lensProblem.testLens(indy.getDoublePosition()), indy.getFitness(0), dim, height, radius, mag, centerLens, centerScreen, segment, g2D);
        }
    }

    private void paintLens(AbstractEAIndividual indy, int dim, int height, double radius, int mag, int centerLens, int centerScreen, int segment, Graphics2D g2D) {
        if (indy != null) {
            paintLens(indy.getDoublePosition(), lensProblem.testLens(indy.getDoublePosition()), indy.getFitness(0), dim, height, radius, mag, centerLens, centerScreen, segment, g2D);
        }
    }

    private static void paintLens(double[] variables, double[] dots, double fit, int dim, int height, double radius, int mag, int centerLens, int centerScreen,
                                  int segment, Graphics2D g2D) {
        // top and bottom line
        g2D.drawLine(centerLens - (int) (variables[0] * mag), height / 2 - (int) radius * 10, centerLens + (int) (variables[0] * mag), height / 2 - (int) radius * 10);
        g2D.drawLine(centerLens - (int) (variables[dim - 1] * mag), height / 2 + (int) radius * 10, centerLens + (int) (variables[dim - 1] * mag), height / 2 + (int) radius * 10);

        // plot the fitness result
        g2D.drawString("Fitness : " + fit, (int) (variables[0] * mag), 15);

        int currentXPos = height / 2 - (int) radius * 10;
        for (int i = 1; i < variables.length; i++) {
            ///System.out.println("X"+i+": " +  variables[i]);
            // draw the line from the least one to the current on and use 10 as magnifier
            g2D.setPaint(Color.black);
            g2D.drawLine(centerLens - (int) (variables[i - 1] * mag), currentXPos, centerLens - (int) (variables[i] * mag), currentXPos + segment);
            g2D.drawLine(centerLens + (int) (variables[i - 1] * mag), currentXPos, centerLens + (int) (variables[i] * mag), currentXPos + segment);

            // paint the light rays
            g2D.setPaint(Color.red);
            g2D.drawLine(0, currentXPos + segment / 2, centerLens, currentXPos + segment / 2);
            g2D.drawLine(centerLens, currentXPos + segment / 2, centerScreen, height / 2 + (int) (dots[i - 1] * mag));

            currentXPos += segment;
        }
    }

    /**
     * This method updates the painted stuff
     *
     * @param pop The population to use
     */
    @Override
    public void updateView(Population pop, boolean showAllIfPossible) {
        if (showAllIfPossible) {
            for (int i = 0; i < pop.size(); i++) {
                MyLensViewer newView = new MyLensViewer(lensProblem);

                Population newPop = new Population();
                newPop.add(pop.getEAIndividual(i));
                newView.updateView(newPop, false);

                JFrame newFrame = new JFrame("Lens Problem Viewer");
                newFrame.getContentPane().add(newView);
                newFrame.pack();
                newFrame.setVisible(true);
            }
        } else {
            AbstractEAIndividual best = (AbstractEAIndividual) pop.getBestIndividual();
            if (indiesToPaint.size() == 0 || (best).isDominant(indiesToPaint.getBestIndividual())) {
                if (indiesToPaint.size() == 1) {
                    indiesToPaint.set(0, best);
                } else {
                    indiesToPaint.add(best);
                }
                this.paint(this.getGraphics());
            }
        }
    }

}

/**
 *
 */
@eva2.util.annotation.Description("Focusing of a lens is to be optimized.")
public class FLensProblem extends AbstractOptimizationProblem
        implements InterfaceOptimizationProblem, InterfaceHasSolutionViewer, java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4694920294291719310L;
    protected AbstractEAIndividual overallBest = null;
    protected double noise = 0.0;
    protected double xOffset = 0.0;
    protected double yOffset = 0.0;
    transient protected boolean show = false;

    transient private JEFrame problemFrame;
    transient private MyLensViewer lensViewerPanel;
    public double radius = 5;
    public double focalLength = 20;
    public double epsilon = 1.5;
    private boolean useMaterialConst = false;

    public FLensProblem() {
        this.template = new ESIndividualDoubleData();
        if (this.show) {
            this.initProblemFrame();
        }
    }

    public FLensProblem(FLensProblem b) {
        //AbstractOptimizationProblem
        if (b.template != null) {
            this.template = (AbstractEAIndividual) b.template.clone();
        }
        //FLensProblem
        if (b.overallBest != null) {
            this.overallBest = (AbstractEAIndividual) b.overallBest.clone();
        }
        this.problemDimension = b.problemDimension;
        this.noise = b.noise;
        this.xOffset = b.xOffset;
        this.yOffset = b.yOffset;
        this.radius = b.radius;
        this.focalLength = b.focalLength;
        this.epsilon = b.epsilon;
        this.useMaterialConst = b.useMaterialConst;
    }

    @Override
    public Object clone() {
        return new FLensProblem(this);
    }

    /**
     * This method initializes a problem view frame
     */
    public void initProblemFrame() {
        if (this.problemFrame == null) {
            this.problemFrame = new JEFrame("Lens Problem Viewer");
            this.lensViewerPanel = new MyLensViewer(this);
            this.problemFrame.getContentPane().add(this.lensViewerPanel);
            this.problemFrame.pack();
            this.problemFrame.setVisible(true);
        } else {
            this.lensViewerPanel.resetView();
        }
    }

    /**
     * This method gets rid of the problem view frame
     */
    public void disposeProblemFrame() {
        if (this.problemFrame != null) {
            this.problemFrame.dispose();
        }
        this.problemFrame = null;
        this.lensViewerPanel = null;
    }

    /**
     * This method update the content in the current problem
     * view frame depending on the current population.
     *
     * @param population The current population.
     */
    public void updateProblemFrame(Population population) {
        if (this.lensViewerPanel != null) {
            this.lensViewerPanel.updateView(population, false);
        }
    }

    /**
     * This method initializes the Problem to log multiruns
     */
    @Override
    public void initializeProblem() {
        this.overallBest = null;
        if (this.show) {
            this.initProblemFrame();
        }
    }

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be initialized
     */
    @Override
    public void initializePopulation(Population population) {
        this.overallBest = null;
        ((InterfaceDataTypeDouble) this.template).setDoubleDataLength(this.problemDimension);
        // set the range
        double[][] range = new double[this.problemDimension][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = 0.1;
            range[i][1] = 5.0;
        }
        ((InterfaceDataTypeDouble) this.template).setDoubleRange(range);

        AbstractOptimizationProblem.defaultInitializePopulation(population, template, this);
        if (this.show) {
            this.initProblemFrame();
        }
    }

    @Override
    public void evaluatePopulationEnd(Population pop) {
        if (this.show) {
            this.updateProblemFrame(pop);
        }
    }

    /**
     * This method evaluate a single individual and sets the fitness values
     *
     * @param individual The individual that is to be evaluated
     */
    @Override
    public void evaluate(AbstractEAIndividual individual) {
        double[] x;
        double[] fitness;

        x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
        System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
        for (int i = 0; i < x.length; i++) {
            x[i] -= this.xOffset;
        }
        fitness = this.doEvaluation(x);
        for (int i = 0; i < fitness.length; i++) {
            // add noise to the fitness
            fitness[i] += RNG.gaussianDouble(this.noise);
            fitness[i] += this.yOffset;
            // set the fitness of the individual
            individual.setFitnessAt(i, fitness[i]);
        }
        if ((this.overallBest == null) || (this.overallBest.getFitness(0) > individual.getFitness(0))) {
            this.overallBest = (AbstractEAIndividual) individual.clone();
        }
    }

    /**
     * Ths method allows you to evaluate a simple bit string to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    public double[] doEvaluation(double[] x) {
        double fitness = 0;
        double[] ret = new double[1];

        // set a minimum value for the thickness of the lens
        for (int i = 0; i < x.length; i++) {
            if (x[i] < 0.1) {
                x[i] = 0.1;
            }
        }

        double[] tmpFit = this.testLens(x);
        for (int i = 0; i < tmpFit.length; i++) {
            fitness += Math.pow(tmpFit[i], 2);
        }

        // Here the thickness of the middle segment of the lens	is added to the fitness
        // to permit the optimization to reduce the overall thickness of the lens
        if (this.useMaterialConst) {
            fitness += x[(x.length / 2)];
        }

        ret[0] = fitness;
        return ret;
    }

    /**
     * this method will return the deviations
     *
     * @param x The lens
     * @return double[]
     */
    public double[] testLens(double[] x) {
        double segmentHeight = 2 * radius / (x.length - 1);
        double[] result = new double[x.length - 1];
        // Computation of fitness. Uses an approximation for very thin lenses.
        // The fitness is the sum over all segments of the deviation from the center
        // of focus of a beam running through a segment.
        for (int i = 1; i < x.length; i++) {
            result[i - 1] = radius - segmentHeight / 2 - segmentHeight * (i - 1) - focalLength / segmentHeight * (epsilon - 1) * (x[i] - x[i - 1]);
        }
        return result;
    }

    /**
     * This method allows you to output a string that describes a found solution
     * in a way that is most suitable for a given problem.
     *
     * @param individual The individual that is to be shown.
     * @return The description.
     */
    @Override
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
        this.evaluate(individual);
        String result = "FX problem:\n";
        result += individual.getStringRepresentation() + "\n";
        result += "Y = " + individual.getFitness(0);
        return result;
    }

    /**
     * This method allows you to output a string that describes a found solution
     * in a way that is most suiteable for a given problem.
     *
     * @param optimizer The individual that is to be shown.
     * @return The description.
     */
    public String getFinalReportOn(InterfaceOptimizer optimizer) {
        String result = optimizer.getStringRepresentation() + "\n";
        result += this.getSolutionRepresentationFor(this.overallBest);
        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    @Override
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
        String result = "";

        result += "FX Problem:\n";
        result += "Here the individual codes a vector of real number x and FX(x)= x is to be minimized.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.noise + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }

    /**
     * This method allows you to request a graphical representation for a given
     * individual.
     */
    @Override
    public JComponent drawIndividual(AbstractEAIndividual indy) {
        JTextArea tindy = new JTextArea(indy.getStringRepresentation());
        JScrollPane pindy = new JScrollPane(tindy);
        tindy.setEditable(false);
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.add(tindy, BorderLayout.NORTH);
        result.add(pindy, BorderLayout.CENTER);
        return result;
    }

    public static void main(String[] args) {
        System.out.println("TEST");
        FLensProblem f = new FLensProblem();
        System.out.println("Working Dir " + System.getProperty("user.dir"));
        StandaloneOptimization program = new StandaloneOptimization();
        OptimizationParameters GO = program.getGOParameters();
        GO.setProblem(f);
        RNG.setRandomSeed(1);
        program.initFrame();
        program.setShow(true);
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Lens Problem";
    }

    /**
     * This method allows you to choose how much noise is to be added to the
     * fitness. This can be used to make the optimization problem more difficult.
     *
     * @param noise The sigma for a gaussian random number.
     */
    public void setNoise(double noise) {
        if (noise < 0) {
            noise = 0;
        }
        this.noise = noise;
    }

    public double getNoise() {
        return this.noise;
    }

    public String noiseTipText() {
        return "Noise level on the fitness value.";
    }

    /**
     * This method allows you to set/get an offset for decision variables.
     *
     * @param XOffSet The offset for the decision variables.
     */
    public void setXOffSet(double XOffSet) {
        this.xOffset = XOffSet;
    }

    public double getXOffSet() {
        return this.xOffset;
    }

    public String xOffSetTipText() {
        return "Choose an offset for the decision variable.";
    }

    /**
     * This method allows you to set/get the offset for the
     * objective value.
     *
     * @param YOffSet The offset for the objective value.
     */
    public void setYOffSet(double YOffSet) {
        this.yOffset = YOffSet;
    }

    public double getYOffSet() {
        return this.yOffset;
    }

    public String yOffSetTipText() {
        return "Choose an offset for the objective value.";
    }

    /**
     * This method allows you to toggle the solution representation.
     *
     * @param show Whether to show the result or not
     */
    public void setShow(boolean show) {
        this.show = show;
        if (this.show) {
            this.initProblemFrame();
        } else {
            this.disposeProblemFrame();
        }
    }

    public boolean getShow() {
        return this.show;
    }

    public String showTipText() {
        return "Toggle the visualization of the solution.";
    }

    /**
     * This method allows you to toggle the use of the material constraint.
     *
     * @param show Whether to show the result or not
     */
    public void setUseMaterialConst(boolean show) {
        this.useMaterialConst = show;
    }

    public boolean getUseMaterialConst() {
        return this.useMaterialConst;
    }

    public String useMaterialConstTipText() {
        return "Toggle the use of the material constraint.";
    }

    /**
     * This method allows you to choose the EA individual
     *
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.template = (AbstractEAIndividual) indy;
    }

    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble) this.template;
    }

    /**
     * This method allows you to set the EA individual type
     * Beware: Trap!
     *
     * @param indy The new EA individual type.
     */
    public void setEAIndividualTrap(AbstractEAIndividual indy) {
        if (indy instanceof InterfaceDataTypeDouble) {
            this.setEAIndividual((InterfaceDataTypeDouble) indy);
        }
    }

    /*
     * (non-Javadoc)
     * @see eva2.problems.InterfaceHasSolutionViewer#getSolutionViewer()
     */
    @Override
    public InterfaceSolutionViewer getSolutionViewer() {
        return lensViewerPanel;
    }
}