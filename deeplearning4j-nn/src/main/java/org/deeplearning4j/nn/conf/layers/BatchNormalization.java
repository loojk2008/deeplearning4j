package org.deeplearning4j.nn.conf.layers;

import lombok.*;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.ParamInitializer;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToCnnPreProcessor;
import org.deeplearning4j.nn.params.BatchNormalizationParamInitializer;
import org.deeplearning4j.optimize.api.IterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Collection;
import java.util.Map;

/**
 * Batch normalization configuration
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder
public class BatchNormalization extends FeedForwardLayer {
    protected double decay;
    protected double eps;
    @Deprecated
    protected boolean useBatchMean;
    protected double gamma;
    protected double beta;
    protected boolean lockGammaBeta;

    private BatchNormalization(Builder builder) {
        super(builder);
        this.decay = builder.decay;
        this.eps = builder.eps;
        this.useBatchMean = builder.useBatchMean;
        this.gamma = builder.gamma;
        this.beta = builder.beta;
        this.lockGammaBeta = builder.lockGammaBeta;
    }

    @Override
    public BatchNormalization clone() {
        BatchNormalization clone = (BatchNormalization) super.clone();
        return clone;
    }

    @Override
    public Layer instantiate(NeuralNetConfiguration conf, Collection<IterationListener> iterationListeners, int layerIndex, INDArray layerParamsView, boolean initializeParams) {
        org.deeplearning4j.nn.layers.normalization.BatchNormalization ret
                = new org.deeplearning4j.nn.layers.normalization.BatchNormalization(conf);
        ret.setListeners(iterationListeners);
        ret.setIndex(layerIndex);
        ret.setParamsViewArray(layerParamsView);
        Map<String, INDArray> paramTable = initializer().init(conf, layerParamsView, initializeParams);
        ret.setParamTable(paramTable);
        ret.setConf(conf);
        return ret;
    }

    @Override
    public ParamInitializer initializer() {
        return BatchNormalizationParamInitializer.getInstance();
    }


    @Override
    public InputType getOutputType(InputType inputType) {
        if (inputType == null) {
            throw new IllegalStateException("Invalid input type: Batch norm layer expected input of type CNN, got " + inputType);
        }

        //Can handle CNN, flat CNN or FF input formats only
        switch (inputType.getType()) {
            case FF:
            case CNN:
            case CNNFlat:
                return inputType; //OK
            default:
                throw new IllegalStateException("Invalid input type: Batch norm layer expected input of type CNN, CNN Flat or FF, got " + inputType);
        }
    }

    @Override
    public void setNIn(InputType inputType, boolean override) {
        if (nIn <= 0 || override) {
            switch (inputType.getType()) {
                case FF:
                    nIn = ((InputType.InputTypeFeedForward) inputType).getSize();
                    break;
                case CNN:
                    nIn = ((InputType.InputTypeConvolutional) inputType).getDepth();
                    break;
                case CNNFlat:
                    nIn = ((InputType.InputTypeConvolutionalFlat) inputType).getDepth();
                default:
                    throw new IllegalStateException("Invalid input type: Batch norm layer expected input of type CNN, CNN Flat or FF, got " + inputType);
            }
            nOut = nIn;
        }
    }

    @Override
    public InputPreProcessor getPreProcessorForInputType(InputType inputType) {
        if (inputType.getType() == InputType.Type.CNNFlat) {
            InputType.InputTypeConvolutionalFlat i = (InputType.InputTypeConvolutionalFlat) inputType;
            return new FeedForwardToCnnPreProcessor(i.getHeight(), i.getWidth(), i.getDepth());
        }

        return null;
    }

    @AllArgsConstructor
    public static class Builder extends FeedForwardLayer.Builder<Builder> {
        protected double decay = 0.9;
        protected double eps = 1e-5;
        @Deprecated //useBatchMean = false ->
        protected boolean useBatchMean = true; // TODO auto set this if layer conf is batch
        protected boolean lockGammaBeta = false;
        protected double gamma = 1.0;
        protected double beta = 0.0;

        @Deprecated
        public Builder(double decay, boolean useBatchMean) {
            this.decay = decay;
            this.useBatchMean = useBatchMean;
        }

        public Builder(double gamma, double beta) {
            this.gamma = gamma;
            this.beta = beta;
        }

        public Builder(double gamma, double beta, boolean lockGammaBeta) {
            this.gamma = gamma;
            this.beta = beta;
            this.lockGammaBeta = lockGammaBeta;
        }

        public Builder(boolean lockGammaBeta) {
            this.lockGammaBeta = lockGammaBeta;
        }

        public Builder() {
        }

        /**
         * Used only when 'true' is passed to {@link #lockGammaBeta(boolean)}. Value is not used otherwise.<br>
         * Default: 1.0
         *
         * @param gamma    Gamma parameter for all activations, used only with locked gamma/beta configuration mode
         */
        public Builder gamma(double gamma) {
            this.gamma = gamma;
            return this;
        }

        /**
         * Used only when 'true' is passed to {@link #lockGammaBeta(boolean)}. Value is not used otherwise.<br>
         * Default: 0.0
         *
         * @param beta    Beta parameter for all activations, used only with locked gamma/beta configuration mode
         */
        public Builder beta(double beta) {
            this.beta = beta;
            return this;
        }

        /**
         * Epsilon value for batch normalization; small floating point value added to variance
         * (algorithm 1 in http://arxiv.org/pdf/1502.03167v3.pdf) to reduce/avoid underflow issues.<br>
         * Default: 1e-5
         *
         * @param eps Epsilon values to use
         */
        public Builder eps(double eps) {
            this.eps = eps;
            return this;
        }

        /**
         * At test time: we can use a global estimate of the mean and variance, calculated using a moving average
         * of the batch means/variances. This moving average is implemented as:<br>
         * globalMeanEstimate = decay * globalMeanEstimate + (1-decay) * batchMean<br>
         * globalVarianceEstimate = decay * globalVarianceEstimate + (1-decay) * batchVariance<br>
         *
         * @param decay Decay value to use for global stats calculation
         */
        public Builder decay(double decay) {
            this.decay = decay;
            return this;
        }

        /**
         * If set to true: lock the gamma and beta parameters to the values for each activation, specified by
         * {@link #gamma(double)} and {@link #beta(double)}. Default: false -> learn gamma and beta parameter values
         * during network training.
         *
         * @param lockGammaBeta If true: use fixed beta/gamma values. False: learn during
         */
        public Builder lockGammaBeta(boolean lockGammaBeta) {
            this.lockGammaBeta = lockGammaBeta;
            return this;
        }

        @Override
        public BatchNormalization build() {
            return new BatchNormalization(this);
        }
    }

}
