package com.ccc.deeplearning.iterativereduce.akka;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.jblas.DoubleMatrix;

import com.ccc.deeplearning.nn.BaseMultiLayerNetwork;
import com.ccc.deeplearning.nn.activation.ActivationFunction;
import com.ccc.deeplearning.scaleout.conf.Conf;
import com.ccc.deeplearning.scaleout.conf.DeepLearningConfigurable;
import com.ccc.deeplearning.scaleout.iterativereduce.multi.ComputableWorkerImpl;
import com.ccc.deeplearning.scaleout.iterativereduce.multi.UpdateableImpl;

public class ComputableWorkerAkka extends ComputableWorkerImpl implements DeepLearningConfigurable {

	private BaseMultiLayerNetwork network;
	private DoubleMatrix combinedInput;
	int fineTuneEpochs;
	int preTrainEpochs;
	boolean useRegularization;
	int[] hiddenLayerSizes;
	int numOuts;
	int numIns;
	double momentum = 0.0;
	int numHiddenNeurons;
	long seed;
	double learningRate;
	double corruptionLevel;
	ActivationFunction activation;
	int[] rows;
	private boolean iterationComplete;
	private int currEpoch;
	private DoubleMatrix outcomes;
	Object[] extraParams;

	public ComputableWorkerAkka(DoubleMatrix whole,DoubleMatrix outcomes,int[] rows) {
		combinedInput = whole.getRows(rows);
		this.rows = rows;
		this.outcomes = outcomes.getRows(rows);
	}

	@Override
	public UpdateableImpl compute(List<UpdateableImpl> records) {
		return compute();
	}

	@Override
	public UpdateableImpl compute() {
		network.trainNetwork(combinedInput, outcomes,extraParams);
		return new UpdateableImpl(network);
	}

	@Override
	public boolean incrementIteration() {
		currEpoch++;
		return false;
	}

	@Override
	public void setup(Conf conf) {
		hiddenLayerSizes = conf.getLayerSizes();
		numOuts = conf.getnOut();
		numIns = conf.getnIn();
		numHiddenNeurons = hiddenLayerSizes.length;
		seed = conf.getSeed();
		this.useRegularization = conf.isUseRegularization();
		momentum = conf.getMomentum();
		this.activation = conf.getFunction();

		RandomGenerator rng = new MersenneTwister(conf.getSeed());
		network = new BaseMultiLayerNetwork.Builder<>()
				.numberOfInputs(numIns).numberOfOutPuts(numOuts)
				.withActivation(activation)
				.hiddenLayerSizes(hiddenLayerSizes).withRng(rng)
				.useRegularization(useRegularization).withMomentum(momentum)
				.withClazz(conf.getMultiLayerClazz()).build();


		learningRate = conf.getPretrainLearningRate();
		preTrainEpochs = conf.getPretrainEpochs();
		fineTuneEpochs = conf.getFinetuneEpochs();
		corruptionLevel = conf.getCorruptionLevel();
		extraParams = conf.getDeepLearningParams();
	}


}
