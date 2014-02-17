package com.ccc.deeplearning.sda.jblas.mnist;

import java.io.IOException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jblas.DoubleMatrix;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ccc.deeplearning.base.DeepLearningTest;
import com.ccc.deeplearning.datasets.DataSet;
import com.ccc.deeplearning.datasets.iterator.impl.MnistDataSetIterator;
import com.ccc.deeplearning.eval.Evaluation;
import com.ccc.deeplearning.sda.StackedDenoisingAutoEncoder;
import com.ccc.deeplearning.util.MatrixUtil;

public class MnistSdaTest extends DeepLearningTest {

	private static Logger log = LoggerFactory.getLogger(MnistSdaTest.class);

	@Test
	public void testMnist() throws IOException {
		MnistDataSetIterator iter = new MnistDataSetIterator(50,50);
		RandomGenerator rng = new MersenneTwister(123);


		DataSet first = iter.next();






		int numIns = first.getFirst().columns;
		int numLabels = first.getSecond().columns;
		int[] layerSizes = {600,600,600};


		double lr = 0.01;


		StackedDenoisingAutoEncoder sda = new StackedDenoisingAutoEncoder.Builder().
				numberOfInputs(numIns).renderWeights(500)
				.numberOfOutPuts(numLabels).withRng(rng).
				hiddenLayerSizes(layerSizes).build();

		
		DoubleMatrix data1 = first.getFirst().dup();
		DoubleMatrix outcomes = first.getSecond().dup();
		do {
			sda.pretrain(data1, lr, 0.3, 1000);
			sda.finetune(outcomes, lr,1000);

			Evaluation eval = new Evaluation();
			log.info("BEGIN EVAL ON " + first.numExamples());
			//	while(iter.hasNext()) {

			DoubleMatrix predicted = sda.predict(data1);
			log.info("Predicted\n " + predicted.toString().replaceAll(";","\n"));

			eval.eval(first.getSecond(), predicted);
			log.info(eval.stats());
			log.info("Loss is " + sda.negativeLogLikelihood());
			if(iter.hasNext())
				first = iter.next();
		}while(iter.hasNext());






	}

}
