package eva2.problems;

import eva2.gui.BeanInspector;

import java.util.BitSet;
import java.util.concurrent.Semaphore;

/**
 * This implements a thread acting as a mediator between EvA and Matlab. Thanks to the idea
 * of Henning Schmidt!
 * As Java calling Matlab directly causes problems (due to Matlabs single-threadedness), Matlab
 * now starts a mediator thread which receives the data necessary to perform the evaluation in matlab
 * from the optimization thread. On receiving this "question" from the optimizer, the mediator thread
 * finishes, returning to Matlab and signaling that there is work to do.
 * However, the mediator object remains persistent, and the optimization thread keeps running
 * and waits for the mediator to signal that there is a result, which happens if Matlab calls setAnswer().
 * Then the optimizer thread resumes, while Matlab has to restart the mediator thread, so that it may
 * be informed about the next question, and so on. I haven't checked how much performance is lost compared
 * to the earlier, asynchronous version, but it feels similar, a difference being that both CPU's
 * are now at 100% load, which is because two threads are running (and always at least waiting actively).
 * Adding sleep time reduces CPU load a lot but reduces efficiency badly at the same time, probably because
 * there's so much going on. For cases where the evaluation function is very time-consuming, adding sleep time
 * might be an option.
 */
public class MatlabEvalMediator {
    volatile Boolean requesting = false;
    volatile private Boolean fin = false;
    volatile Object question = null;
    volatile double[] answer = null;
    volatile boolean quit = false;
    volatile Object optSolution = null;
    volatile Object[] optSolSet = null;
    volatile Semaphore requests;
    int runID = -1;
    volatile MatlabProblem mp = null;
    // no good: even when waiting for only 1 ms the Matlab execution time increases by a factor of 5-10
    private int sleepTime = 5;

    /**
     * Constructor with integer argument for the sleep time in between requests. Values higher than 0 reduce
     * cpu load of the waiting thread but may also reduce allover runtime for fast evaluation functions.
     *
     * @param threadSleepTime
     */
    public MatlabEvalMediator(int threadSleepTime) {
        sleepTime = threadSleepTime;
        requests = new Semaphore(0);
    }

    /**
     * Constructor with sleep time set to zero.
     */
    public MatlabEvalMediator() {
        sleepTime = 0;
        requests = new Semaphore(0);
    }

    public void setMatlabProblem(MatlabProblem theMP) {
        mp = theMP;
        logMP("setting MP " + theMP + " for MEM " + this + "\n");
    }

    /**
     * Request evaluation from Matlab for the given params.
     *
     * @param x
     * @return
     */
    double[] requestEval(MatlabProblem mp, Object x) {
        this.mp = mp;
        question = x;
        if (question.getClass().isArray()) {
            if (question == null) {
                System.err.println("Error: requesting evaluation for null array!");
            }
        } else { // if its not an array, it must be a BitSet
            if (!(x instanceof BitSet)) {
                System.err.println("Error, requesting evaluation for invalid data type! " + question.getClass());
            }
        }
        synchronized (requesting) { //MdP
            if (requesting) {
                String msg = "Warning: already in requesting state when request arrived!";
                System.err.println(msg);
                logMP(msg);
            }
            requesting = true;
        }

        try {
            requests.acquire();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logMP("-- Requesting done\n");
        // matlab is finished, answer is here
        return getAnswer(); // return to JE with answer
    }

    public String getState() {
        return "ID: " + runID + ", qu: " + BeanInspector.toString(question) + " quit,fin,req " + quit + "," + fin + "," + requesting;
    }

    /**
     * Wait loop, wait until the MatlabProblem requests an evaluation (or finishes), then return.
     * For debugging, an integer ID for this run may be provided.
     */
    public void run(int id) {
        logMPOrSysOut("## MEM start run " + id);
        runID = id;
        int k = 0;
        int mod = 25;
        while (!requesting && !isFinished() && !quit) {
            // wait for JE to pose a question or finish all
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                }
            }
            k++;
            if ((k % mod) == 0) {
                logMPOrSysOut("MEM waiting for JE to ask... (" + mod + ") " + getState());
                mod *= 2;
                if (mod <= 0) {
                    mod = Integer.MAX_VALUE;
                }
            }
        }
        if (requesting) {
            logMPOrSysOut("-- MEM Request arrived in MP thread " + runID);
        } else {
            logMPOrSysOut("-- MEM finished or quit " + runID);
        }
        // requesting is true, now finish and let Matlab work
    }

    /**
     * Wait loop, wait until the MatlabProblem requests an evaluation (or finishes), then return.
     * Calls the {@link #run(int)} method with ID zero.
     */
    public void run() {
        run(0);
    }

    private void logMPOrSysOut(String msg) {
        logMP(msg);
    }

    private void logMP(String msg) {
        if (mp != null) {
            mp.log(msg + "\n");
        }
    }

    /**
     * Cancel waiting in any case.
     */
    public void quit() {
        quit = true;
    }

    /**
     * To be called from Matlab.
     *
     * @return
     */
    public Object getQuestion() {
        if (mp != null) {
            logMP("-- Question: " + BeanInspector.toString(question) + "\n");
        }
        return question;
    }

    double[] getAnswer() {
        if (mp != null) {
            logMP("-- mediator delivering " + BeanInspector.toString(answer) + "\n");
        }
        return answer;
    }

    /**
     * To be called from Matlab giving the result of the question.
     *
     * @param y
     */
    public void setAnswer(double[] y) {
        synchronized (requesting) {
            if (!requesting) {
                String msg = "Error: not in requesting state when answer arrived!!";
                System.err.println(msg);
                logMP(msg);
            }
            if (y == null) {
                System.err.println("Error: Matlab function returned null array - this is bad.");
                System.err.println("X-value was " + BeanInspector.toString(getQuestion()));
            }
            answer = y;
            requesting = false; // answer is finished, break request loop
            requests.release();
            logMP("-- setAnswer: " + BeanInspector.toString(y) + ", req state is " + requesting + "\n");
        }
    }

    void setFinished(boolean val) {
        synchronized (fin) {
            if (fin && val) {
                String msg = "Error: already finished when setFinished(true) was called!";
                System.err.println(msg);
                logMP(msg);
            }
            fin = val;
            logMPOrSysOut("MEM setFinished ok");
        }
    }

    /**
     * To be called from Matlab signalling when optimizaton is completely finished.
     *
     * @return
     */
    public boolean isFinished() {
        return fin;
    }

    void setSolution(Object sol) {
        optSolution = sol;
    }

    void setSolutionSet(double[][] solSet) {
        optSolSet = solSet;
    }

    void setSolutionSet(BitSet[] solSet) {
        optSolSet = solSet;
    }

    void setSolutionSet(int[][] solSet) {
        optSolSet = solSet;
    }

    /**
     * Matlab may retrieve result.
     *
     * @return
     */
    public Object getSolution() {
        return optSolution;
    }

    /**
     * Matlab may retrieve result as Object[] containing either double[] or int[].
     *
     * @return
     */
    public Object getSolutionSet() {
        return optSolSet;
    }
}