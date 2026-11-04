package ai.flow.modeld;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Map;

public abstract class ModelRunner{
    public void init(Map<String, int[]> inputShapes, Map<String, int[]> outputShapes){}
    public void warmup(){}
    public void run(Map<String, INDArray> inputMap, int lastFrame){}
    public void dispose(){}
}
