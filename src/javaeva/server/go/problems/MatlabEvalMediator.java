package javaeva.server.go.problems;

import javaeva.gui.BeanInspector;

/**
 * This implements a thread acting as a mediator between JavaEvA and Matlab. Thanks to the idea 
 * of Henning Schmidt!
 * As Java calling Matlab directly causes problems (due to Matlabs single-threadedness), Matlab
 * now starts a mediator thread which receives the data necessary to perform the evaluation in matlab
 * from the optimization thread. On receiving this "question" from the optimizer, the mediator thread
 * finishes, returning to Matlab and signaling that there is work to do. 
 * However, the mediator object remains persistent, and the optimization thread keeps running 
 * and waits for the mediator to signal that there is a result, which happens if Matlab calls setAnswer().
 * Then the optimizer thread resumes, while Matlab has to restart the mediator thread, so that it may
 * be informed about the next question, and so on. I havent checked how much performance is lost compared
 * to the earlier, asynchronous version, but it feels similar, a difference being that both cpu's
 * are now at 100% load, which is because two threads are running (and always at least waiting actively).
 * Adding sleep time reduces CPU load a lot but reduces efficiency badly at the same time, probably because
 * theres so much going on. For cases where the evaluation function is very time-consuming, adding sleep time
 * might be an option.
 *   
 * @author mkron
 *
 */
public class MatlabEvalMediator implements Runnable {
	volatile boolean requesting = false;
//	final static boolean TRACE = false;
	volatile boolean fin = false;
	volatile double[] question = null;
	volatile double[] answer = null;
	boolean quit = false;
	volatile double[] optSolution = null;
	volatile double[][] optSolSet = null;
//	MatlabProblem mp = null;
	// no good: even when waiting for only 1 ms the Matlab execution time increases by a factor of 5-10
	final static int sleepTime = 0;

	/**
	 * Request evaluation from Matlab for the given params.
	 * 
	 * @param x
	 * @return
	 */
	double[] requestEval(MatlabProblem mp, double[] x) {
//		this.mp = mp;
		question = x;
		requesting = true;
//		int k=0;
//		System.out.println("Requesting eval for " + BeanInspector.toString(x) + ", req state is " + requesting + "\n"); 
		while (requesting && !quit) {
			// 	wait for matlab to answer the question
			if (sleepTime > 0) try { Thread.sleep(sleepTime); } catch(Exception e) {};
//			if ((k%100)==0) {
//				System.out.println("waiting for matlab to answer...");
//			}
//			k++;
		}
//		System.out.println("Requesting done \n");
		// matlab is finished, answer is here
		return getAnswer(); // return to JE with answer
	}

	/**
	 * Wait loop, wait until the MatlabProblem requests an evaluation (or finishes), then return.
	 */
	public void run() {
//		int k=0;
		while (!requesting && !isFinished() && !quit) {
			// wait for JE to pose a question or finish all
			if (sleepTime > 0) try { Thread.sleep(sleepTime); } catch(Exception e) {};
//			if ((k%100)==0) {
//				System.out.println("waiting for JE to ask...");
//			}
//			k++;
		}
//		System.out.println("-- Request arrived in MP thread\n");
		// requesting is true, now finish and let Matlab work
	}

	/**
	 * Cancel waiting in any case.
	 */
	public void quit() {
//		System.out.println("IN QUIT!");
		quit = true;
	}
	
	/**
	 * To be called from Matlab.
	 * @return
	 */
	public double[] getQuestion() {
//		mp.log("-- Question: " + BeanInspector.toString(question) + "\n");
		return question;
	}

	double[] getAnswer() {
		return answer;
	}

	/**
	 * To be called from Matlab giving the result of the question.
	 * 
	 * @param y
	 */
	public void setAnswer(double[] y) {
//		mp.log("-- setAnswer: " + BeanInspector.toString(y) + "\n"); 
		answer = y;
		requesting = false; // answer is finished, break request loop
	}

	void setFinished(boolean val) {
		fin = val;
	}

	/**
	 * To be called from Matlab signalling when optimizaton is completely finished.
	 * @return
	 */
	public boolean isFinished() {
		return fin;
	}
	
	void setSolution(double[] sol) {
		optSolution = sol;
	}
	
	void setSolutionSet(double[][] solSet) {
		optSolSet = solSet;
	}
	
	/**
	 * Matlab may retrieve result.
	 * @return
	 */
	public double[] getSolution() {
		return optSolution;
	}
	
	/**
	 * Matlab may retrieve result.
	 * @return
	 */
	public double[][] getSolutionSet() {
		return optSolSet;
	}
}