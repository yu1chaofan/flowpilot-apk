//package ai.flow.modeld.messages;
//
//import static ai.flow.modeld.CommonModelF2.LEAD_TRAJ_LEN;
//import static ai.flow.modeld.CommonModelF3.LEAD_MHP_SELECTION;
//import static ai.flow.modeld.CommonModelF3.TRAJECTORY_SIZE;
//import static ai.flow.modeld.Parser.sigmoid;
//
//import org.capnproto.PrimitiveList;
//import org.capnproto.StructList;
//
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.stream.Stream;
//
//import ai.flow.definitions.Definitions;
//import ai.flow.definitions.MessageBase;
//import ai.flow.modeld.CommonModelF2;
//import ai.flow.modeld.ModelOutput;
//
//public class MsgModelDataV2F3 extends MessageBase {
//
//    public Definitions.ModelDataV2.Builder modelDataV2;
//
//    public Definitions.XYZTData.Builder position;
//    public Definitions.XYZTData.Builder velocity;
//    public Definitions.XYZTData.Builder acceleration;
//    public Definitions.XYZTData.Builder orientation;
//    public Definitions.XYZTData.Builder orientationRate;
//    public PrimitiveList.Float.Builder positionX;
//    public PrimitiveList.Float.Builder positionY;
//    public PrimitiveList.Float.Builder positionZ;
//    public PrimitiveList.Float.Builder positionT;
//    public PrimitiveList.Float.Builder positionXStd;
//    public PrimitiveList.Float.Builder positionYStd;
//    public PrimitiveList.Float.Builder positionZStd;
//
//    public PrimitiveList.Float.Builder velocityX;
//    public PrimitiveList.Float.Builder velocityY;
//    public PrimitiveList.Float.Builder velocityZ;
//    public PrimitiveList.Float.Builder velocityT;
//
//    public PrimitiveList.Float.Builder accelerationX;
//    public PrimitiveList.Float.Builder accelerationY;
//    public PrimitiveList.Float.Builder accelerationZ;
//    public PrimitiveList.Float.Builder accelerationT;
//
//    public PrimitiveList.Float.Builder orientationX;
//    public PrimitiveList.Float.Builder orientationY;
//    public PrimitiveList.Float.Builder orientationZ;
//    public PrimitiveList.Float.Builder orientationT;
//
//    public PrimitiveList.Float.Builder orientationRateX;
//    public PrimitiveList.Float.Builder orientationRateY;
//    public PrimitiveList.Float.Builder orientationRateZ;
//    public PrimitiveList.Float.Builder orientationRateT;
//
//    public StructList.Builder<Definitions.XYZTData.Builder> laneLines;
//    public Definitions.XYZTData.Builder laneLine1;
//    public Definitions.XYZTData.Builder laneLine2;
//    public Definitions.XYZTData.Builder laneLine3;
//    public Definitions.XYZTData.Builder laneLine4;
//
//    public PrimitiveList.Float.Builder laneLineX1;
//    public PrimitiveList.Float.Builder laneLineY1;
//    public PrimitiveList.Float.Builder laneLineZ1;
//    public PrimitiveList.Float.Builder laneLineT1;
//    public PrimitiveList.Float.Builder laneLineXStd1;
//    public PrimitiveList.Float.Builder laneLineYStd1;
//    public PrimitiveList.Float.Builder laneLineZStd1;
//    public PrimitiveList.Float.Builder laneLine1Stds;
//
//    public PrimitiveList.Float.Builder laneLineX2;
//    public PrimitiveList.Float.Builder laneLineY2;
//    public PrimitiveList.Float.Builder laneLineZ2;
//    public PrimitiveList.Float.Builder laneLineT2;
//    public PrimitiveList.Float.Builder laneLineXStd2;
//    public PrimitiveList.Float.Builder laneLineYStd2;
//    public PrimitiveList.Float.Builder laneLineZStd2;
//    public PrimitiveList.Float.Builder laneLine2Stds;
//
//    public PrimitiveList.Float.Builder laneLineX3;
//    public PrimitiveList.Float.Builder laneLineY3;
//    public PrimitiveList.Float.Builder laneLineZ3;
//    public PrimitiveList.Float.Builder laneLineT3;
//    public PrimitiveList.Float.Builder laneLineXStd3;
//    public PrimitiveList.Float.Builder laneLineYStd3;
//    public PrimitiveList.Float.Builder laneLineZStd3;
//    public PrimitiveList.Float.Builder laneLine3Stds;
//
//    public PrimitiveList.Float.Builder laneLineX4;
//    public PrimitiveList.Float.Builder laneLineY4;
//    public PrimitiveList.Float.Builder laneLineZ4;
//    public PrimitiveList.Float.Builder laneLineT4;
//    public PrimitiveList.Float.Builder laneLineXStd4;
//    public PrimitiveList.Float.Builder laneLineYStd4;
//    public PrimitiveList.Float.Builder laneLineZStd4;
//    public PrimitiveList.Float.Builder laneLineProbs;
//    public PrimitiveList.Float.Builder laneLineStds;
//
//    public StructList.Builder<Definitions.XYZTData.Builder> roadEdges;
//    public Definitions.XYZTData.Builder roadEdge1;
//    public Definitions.XYZTData.Builder roadEdge2;
//
//    public PrimitiveList.Float.Builder roadEdgeX1;
//    public PrimitiveList.Float.Builder roadEdgeY1;
//    public PrimitiveList.Float.Builder roadEdgeZ1;
//    public PrimitiveList.Float.Builder roadEdgeT1;
//    public PrimitiveList.Float.Builder roadEdgeXStd1;
//    public PrimitiveList.Float.Builder roadEdgeYStd1;
//    public PrimitiveList.Float.Builder roadEdgeZStd1;
//    public PrimitiveList.Float.Builder roadEdgeX2;
//    public PrimitiveList.Float.Builder roadEdgeY2;
//    public PrimitiveList.Float.Builder roadEdgeZ2;
//    public PrimitiveList.Float.Builder roadEdgeT2;
//    public PrimitiveList.Float.Builder roadEdgeXStd2;
//    public PrimitiveList.Float.Builder roadEdgeYStd2;
//    public PrimitiveList.Float.Builder roadEdgeZStd2;
//    public PrimitiveList.Float.Builder roadEdgeStds;
//
//    public StructList.Builder<Definitions.ModelDataV2.LeadDataV3.Builder> leads;
//    public Definitions.ModelDataV2.LeadDataV3.Builder leads1;
//    public Definitions.ModelDataV2.LeadDataV3.Builder leads2;
//    public Definitions.ModelDataV2.LeadDataV3.Builder leads3;
//
//    public PrimitiveList.Float.Builder leadX1;
//    public PrimitiveList.Float.Builder leadY1;
//    public PrimitiveList.Float.Builder leadV1;
//    public PrimitiveList.Float.Builder leadA1;
//    public PrimitiveList.Float.Builder leadT1;
//    public PrimitiveList.Float.Builder leadXStd1;
//    public PrimitiveList.Float.Builder leadYStd1;
//    public PrimitiveList.Float.Builder leadVStd1;
//    public PrimitiveList.Float.Builder leadAStd1;
//
//    public PrimitiveList.Float.Builder leadX2;
//    public PrimitiveList.Float.Builder leadY2;
//    public PrimitiveList.Float.Builder leadV2;
//    public PrimitiveList.Float.Builder leadA2;
//    public PrimitiveList.Float.Builder leadT2;
//    public PrimitiveList.Float.Builder leadXStd2;
//    public PrimitiveList.Float.Builder leadYStd2;
//    public PrimitiveList.Float.Builder leadVStd2;
//    public PrimitiveList.Float.Builder leadAStd2;
//
//    public PrimitiveList.Float.Builder leadX3;
//    public PrimitiveList.Float.Builder leadY3;
//    public PrimitiveList.Float.Builder leadV3;
//    public PrimitiveList.Float.Builder leadA3;
//    public PrimitiveList.Float.Builder leadT3;
//    public PrimitiveList.Float.Builder leadXStd3;
//    public PrimitiveList.Float.Builder leadYStd3;
//    public PrimitiveList.Float.Builder leadVStd3;
//    public PrimitiveList.Float.Builder leadAStd3;
//
//    public Definitions.ModelDataV2.MetaData.Builder meta;
//    public Definitions.ModelDataV2.DisengagePredictions.Builder disengagePredictions;
//    public PrimitiveList.Float.Builder desireState;
//    public PrimitiveList.Float.Builder desirePredictions;
//    public PrimitiveList.Float.Builder t;
//    public PrimitiveList.Float.Builder brakeDisengageProbs;
//    public PrimitiveList.Float.Builder gasDisengageProbs;
//    public PrimitiveList.Float.Builder steerOverrideProbs;
//    public PrimitiveList.Float.Builder brake3MetersPerSecondSquaredProbs;
//    public PrimitiveList.Float.Builder brake4MetersPerSecondSquaredProbs;
//    public PrimitiveList.Float.Builder brake5MetersPerSecondSquaredProbs;
//
//
//    public MsgModelDataV2F3(ByteBuffer rawMessageBuffer) {
//        super(rawMessageBuffer);
//        initFields();
//        bytesSerializedForm = computeSerializedMsgBytes();
//        initSerializedBuffer();
//    }
//
//    public MsgModelDataV2F3() {
//        super();
//        initFields();
//        bytesSerializedForm = computeSerializedMsgBytes();
//        initSerializedBuffer();
//    }
//
//    private void initFields(){
//        event = messageBuilder.initRoot(Definitions.Event.factory);
//        modelDataV2 = event.initModelV2();
//
//        position = modelDataV2.initPosition();
//        velocity = modelDataV2.initVelocity();
//        acceleration = modelDataV2.initAcceleration();
//        orientation = modelDataV2.initOrientation();
//        orientationRate = modelDataV2.initOrientationRate();
//
//        positionX = position.initX(CommonModelF2.TRAJECTORY_SIZE);
//        positionY = position.initY(CommonModelF2.TRAJECTORY_SIZE);
//        positionZ = position.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        positionT = position.initT(CommonModelF2.TRAJECTORY_SIZE);
//        positionXStd = position.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        positionYStd = position.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        positionZStd = position.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        velocityX = velocity.initX(CommonModelF2.TRAJECTORY_SIZE);
//        velocityY = velocity.initY(CommonModelF2.TRAJECTORY_SIZE);
//        velocityZ = velocity.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        velocityT = velocity.initT(CommonModelF2.TRAJECTORY_SIZE);
//
//        accelerationX = acceleration.initX(CommonModelF2.TRAJECTORY_SIZE);
//        accelerationY = acceleration.initY(CommonModelF2.TRAJECTORY_SIZE);
//        accelerationZ = acceleration.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        accelerationT = acceleration.initT(CommonModelF2.TRAJECTORY_SIZE);
//
//        orientationX = orientation.initX(CommonModelF2.TRAJECTORY_SIZE);
//        orientationY = orientation.initY(CommonModelF2.TRAJECTORY_SIZE);
//        orientationZ = orientation.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        orientationT = orientation.initT(CommonModelF2.TRAJECTORY_SIZE);
//
//        orientationRateX = orientationRate.initX(CommonModelF2.TRAJECTORY_SIZE);
//        orientationRateY = orientationRate.initY(CommonModelF2.TRAJECTORY_SIZE);
//        orientationRateZ = orientationRate.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        orientationRateT = orientationRate.initT(CommonModelF2.TRAJECTORY_SIZE);
//
//        laneLines = modelDataV2.initLaneLines(4);
//        laneLine1 = laneLines.get(0);
//        laneLine2 = laneLines.get(1);
//        laneLine3 = laneLines.get(2);
//        laneLine4 = laneLines.get(3);
//
//        laneLineX1 = laneLine1.initX(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineY1 = laneLine1.initY(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZ1 = laneLine1.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineT1 = laneLine1.initT(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineXStd1 = laneLine1.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineYStd1 = laneLine1.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZStd1 = laneLine1.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        laneLineX2 = laneLine2.initX(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineY2 = laneLine2.initY(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZ2 = laneLine2.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineT2 = laneLine2.initT(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineXStd2 = laneLine2.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineYStd2 = laneLine2.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZStd2 = laneLine2.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        laneLineX3 = laneLine3.initX(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineY3 = laneLine3.initY(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZ3 = laneLine3.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineT3 = laneLine3.initT(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineXStd3 = laneLine3.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineYStd3 = laneLine3.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZStd3 = laneLine3.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        laneLineX4 = laneLine4.initX(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineY4 = laneLine4.initY(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZ4 = laneLine4.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineT4 = laneLine4.initT(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineXStd4 = laneLine4.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineYStd4 = laneLine4.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        laneLineZStd4 = laneLine4.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        laneLineProbs = modelDataV2.initLaneLineProbs(4);
//        laneLineStds = modelDataV2.initLaneLineStds(4);
//
//        roadEdges = modelDataV2.initRoadEdges(2);
//        roadEdge1 = roadEdges.get(0);
//        roadEdge2 = roadEdges.get(1);
//
//        roadEdgeX1 = roadEdge1.initX(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeY1 = roadEdge1.initY(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeZ1 = roadEdge1.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeT1 = roadEdge1.initT(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeXStd1 = roadEdge1.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeYStd1 = roadEdge1.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeZStd1 = roadEdge1.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        roadEdgeX2 = roadEdge2.initX(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeY2 = roadEdge2.initY(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeZ2 = roadEdge2.initZ(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeT2 = roadEdge2.initT(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeXStd2 = roadEdge2.initXStd(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeYStd2 = roadEdge2.initYStd(CommonModelF2.TRAJECTORY_SIZE);
//        roadEdgeZStd2 = roadEdge2.initZStd(CommonModelF2.TRAJECTORY_SIZE);
//
//        roadEdgeStds = modelDataV2.initRoadEdgeStds(2);
//
//        leads = modelDataV2.initLeadsV3(CommonModelF2.LEAD_MHP_SELECTION);
//        leads1 = leads.get(0);
//        leads2 = leads.get(1);
//        leads3 = leads.get(2);
//
//        leadX1 = leads1.initX(LEAD_TRAJ_LEN);
//        leadY1 = leads1.initY(LEAD_TRAJ_LEN);
//        leadV1 = leads1.initV(LEAD_TRAJ_LEN);
//        leadA1 = leads1.initA(LEAD_TRAJ_LEN);
//        leadT1 = leads1.initT(LEAD_TRAJ_LEN);
//        leadXStd1 = leads1.initXStd(LEAD_TRAJ_LEN);
//        leadYStd1 = leads1.initYStd(LEAD_TRAJ_LEN);
//        leadVStd1 = leads1.initVStd(LEAD_TRAJ_LEN);
//        leadAStd1 = leads1.initAStd(LEAD_TRAJ_LEN);
//
//        leadX2 = leads2.initX(LEAD_TRAJ_LEN);
//        leadY2 = leads2.initY(LEAD_TRAJ_LEN);
//        leadV2 = leads2.initV(LEAD_TRAJ_LEN);
//        leadA2 = leads2.initA(LEAD_TRAJ_LEN);
//        leadT2 = leads2.initT(LEAD_TRAJ_LEN);
//        leadXStd2 = leads2.initXStd(LEAD_TRAJ_LEN);
//        leadYStd2 = leads2.initYStd(LEAD_TRAJ_LEN);
//        leadVStd2 = leads2.initVStd(LEAD_TRAJ_LEN);
//        leadAStd2 = leads2.initAStd(LEAD_TRAJ_LEN);
//
//        leadX3 = leads3.initX(LEAD_TRAJ_LEN);
//        leadY3 = leads3.initY(LEAD_TRAJ_LEN);
//        leadV3 = leads3.initV(LEAD_TRAJ_LEN);
//        leadA3 = leads3.initA(LEAD_TRAJ_LEN);
//        leadT3 = leads3.initT(LEAD_TRAJ_LEN);
//        leadXStd3 = leads3.initXStd(LEAD_TRAJ_LEN);
//        leadYStd3 = leads3.initYStd(LEAD_TRAJ_LEN);
//        leadVStd3 = leads3.initVStd(LEAD_TRAJ_LEN);
//        leadAStd3 = leads3.initAStd(LEAD_TRAJ_LEN);
//
//        meta = modelDataV2.initMeta();
//        disengagePredictions = meta.initDisengagePredictions();
//
//        desireState = meta.initDesireState(CommonModelF2.DESIRE_LEN);
//        desirePredictions = meta.initDesirePrediction(4* CommonModelF2.DESIRE_LEN);
//        t = disengagePredictions.initT(5);
//        gasDisengageProbs = disengagePredictions.initGasDisengageProbs(CommonModelF2.NUM_META_INTERVALS);
//        brakeDisengageProbs = disengagePredictions.initBrakeDisengageProbs(CommonModelF2.NUM_META_INTERVALS);
//        steerOverrideProbs = disengagePredictions.initSteerOverrideProbs(CommonModelF2.NUM_META_INTERVALS);
//        brake3MetersPerSecondSquaredProbs = disengagePredictions.initBrake3MetersPerSecondSquaredProbs(CommonModelF2.NUM_META_INTERVALS);
//        brake4MetersPerSecondSquaredProbs = disengagePredictions.initBrake4MetersPerSecondSquaredProbs(CommonModelF2.NUM_META_INTERVALS);
//        brake5MetersPerSecondSquaredProbs = disengagePredictions.initBrake5MetersPerSecondSquaredProbs(CommonModelF2.NUM_META_INTERVALS);
//    }
//
//    public static float[] buildIdxs(float maxVal, int size) {
//        float[] result = new float[size];
//        for (int i = 0; i < size; ++i) {
//            result[i] = (float) (maxVal * ((i / (double) (size - 1)) * (i / (double) (size - 1))));
//        }
//        return result;
//    }
//    static final float[] T_IDXS = buildIdxs(10.0f, TRAJECTORY_SIZE);
//    static final float[] X_IDXS = buildIdxs(192.0f, TRAJECTORY_SIZE);
//
//    public Float[] softmax(ArrayList<Float> vals, PrimitiveList.Float.Builder builder){
//        float max = vals.stream().max(Float::compare).get();
//        float sum = vals.stream()
//                .map(f -> (float) Math.exp(f - max))
//                .reduce(0f, Float::sum);
//        return vals.stream()
//                .map(f -> (float) Math.exp(f - max) / sum)
//                .toArray(Float[]::new);
//    }
//
//    public void fill(ModelOutput model, long timestamp, int frameId,
//                     int frameAge, float frameDropPerc, float modelExecutionTime,
//                     float gpuExecutionTime) {
//        modelDataV2.setFrameId(frameId);
//        modelDataV2.setFrameAge(frameAge);
//        modelDataV2.setFrameDropPerc(frameDropPerc);
//        modelDataV2.setTimestampEof(timestamp);
//        modelDataV2.setGpuExecutionTime(gpuExecutionTime); // this is actually stop sign prob
//        modelDataV2.setModelExecutionTime(modelExecutionTime);
//
//        ModelOutput.ModelOutputPlanPrediction bestPlan = model.plans().prediction().stream()
//                .max(Comparator.comparing(ModelOutput.ModelOutputPlanPrediction::prob)).get();
//
//        // Best plan
//        for (int i = 0; i < CommonModelF2.TRAJECTORY_SIZE; i++) {
//            positionX.set(i, bestPlan.mean().get(i).position().x());
//            positionY.set(i, bestPlan.mean().get(i).position().y());
//            positionZ.set(i, bestPlan.mean().get(i).position().z());
//            positionT.set(i, T_IDXS[i]);
//
//            positionXStd.set(i, (float)Math.exp(bestPlan.std().get(i).position().x()));
//            positionYStd.set(i, (float)Math.exp(bestPlan.std().get(i).position().y()));
//            positionZStd.set(i, (float)Math.exp(bestPlan.std().get(i).position().z()));
//
//            velocityX.set(i, bestPlan.mean().get(i).velocity().x());
//            velocityY.set(i, bestPlan.mean().get(i).velocity().y());
//            velocityZ.set(i, bestPlan.mean().get(i).velocity().z());
//            velocityT.set(i, T_IDXS[i]);
//
//            accelerationX.set(i, bestPlan.mean().get(i).acceleration().x());
//            accelerationY.set(i, bestPlan.mean().get(i).acceleration().y());
//            accelerationZ.set(i, bestPlan.mean().get(i).acceleration().z());
//            accelerationT.set(i, T_IDXS[i]);
//
//            orientationX.set(i, bestPlan.mean().get(i).rotation().x());
//            orientationY.set(i, bestPlan.mean().get(i).rotation().y());
//            orientationZ.set(i, bestPlan.mean().get(i).rotation().z());
//            orientationT.set(i, T_IDXS[i]);
//
//            orientationRateX.set(i, bestPlan.mean().get(i).rotationRate().x());
//            orientationRateY.set(i, bestPlan.mean().get(i).rotationRate().y());
//            orientationRateZ.set(i, bestPlan.mean().get(i).rotationRate().z());
//            orientationRateT.set(i, T_IDXS[i]);
//        }
//
//        // plan_t
//        float[] planT = new float[TRAJECTORY_SIZE];
//        Arrays.fill(planT, Float.NaN);
//        planT[0] = 0.0f;
//
//        for (int xidx = 1, tidx = 0; xidx < TRAJECTORY_SIZE; xidx++) {
//            for (int nextTid = tidx + 1; nextTid < TRAJECTORY_SIZE && bestPlan.mean().get(nextTid).position().x() < X_IDXS[xidx]; nextTid++) {
//                tidx++;
//            }
//            if (tidx == TRAJECTORY_SIZE - 1) {
//                planT[xidx] = T_IDXS[TRAJECTORY_SIZE - 1];
//                break;
//            }
//
//            float currentXVal = bestPlan.mean().get(tidx).position().x();
//            float nextXVal = bestPlan.mean().get(tidx + 1).position().x();
//            float p = (X_IDXS[xidx] - currentXVal) / (nextXVal - currentXVal);
//            planT[xidx] = p * T_IDXS[tidx + 1] + (1 - p) * T_IDXS[tidx];
//        }
//
//        //Lane lines
//        for (int i = 0; i < CommonModelF2.TRAJECTORY_SIZE; i++) {
//            laneLineX1.set(i, X_IDXS[i]);
//            laneLineY1.set(i, model.laneLines().mean().leftFar().get(i).y());
//            laneLineZ1.set(i, model.laneLines().mean().leftFar().get(i).z());
//            laneLineT1.set(i, planT[i]);
//
//            laneLineX2.set(i, X_IDXS[i]);
//            laneLineY2.set(i, model.laneLines().mean().leftNear().get(i).y());
//            laneLineZ2.set(i, model.laneLines().mean().leftNear().get(i).z());
//            laneLineT2.set(i, planT[i]);
//
//            laneLineX3.set(i, X_IDXS[i]);
//            laneLineY3.set(i, model.laneLines().mean().rightNear().get(i).y());
//            laneLineZ3.set(i, model.laneLines().mean().rightNear().get(i).z());
//            laneLineT3.set(i, planT[i]);
//
//            laneLineX4.set(i, X_IDXS[i]);
//            laneLineY4.set(i, model.laneLines().mean().rightFar().get(i).y());
//            laneLineZ4.set(i, model.laneLines().mean().rightFar().get(i).z());
//            laneLineT4.set(i, planT[i]);
//        }
//        laneLineStds.set(0, (float)Math.exp(model.laneLines().std().leftFar().get(0).y()));
//        laneLineStds.set(1, (float)Math.exp(model.laneLines().std().leftNear().get(0).y()));
//        laneLineStds.set(2, (float)Math.exp(model.laneLines().std().rightNear().get(0).y()));
//        laneLineStds.set(3, (float)Math.exp(model.laneLines().std().rightFar().get(0).y()));
//
//        laneLineProbs.set(0, model.laneLines().prob().leftFar().val());
//        laneLineProbs.set(1, model.laneLines().prob().leftNear().val());
//        laneLineProbs.set(2, model.laneLines().prob().rightNear().val());
//        laneLineProbs.set(3, model.laneLines().prob().rightFar().val());
//
//        //Road edges
//        for (int i = 0; i < CommonModelF2.TRAJECTORY_SIZE; i++) {
//            roadEdgeX1.set(i, X_IDXS[i]);
//            roadEdgeY1.set(i, model.roadEdges().mean().left().get(i).y());
//            roadEdgeZ1.set(i, model.roadEdges().mean().left().get(i).z());
//            roadEdgeT1.set(i, planT[i]);
//
//            roadEdgeX2.set(i, X_IDXS[i]);
//            roadEdgeY2.set(i, model.roadEdges().mean().right().get(i).y());
//            roadEdgeZ2.set(i, model.roadEdges().mean().right().get(i).z());
//            roadEdgeT2.set(i, planT[i]);
//        }
//
//        roadEdgeStds.set(0, (float)Math.exp(model.roadEdges().std().left().get(0).y()));
//        roadEdgeStds.set(1, (float)Math.exp(model.roadEdges().std().right().get(0).y()));
//
//
//        //Meta
////        float[] desireState = new float[DESIRE_LEN];
//        Float[] asdf = softmax(model.meta().desireStateProb().raw());
//        desireState.set();
//        softmax(model.meta().desireStateProb().array(), desireState, DESIRE_LEN);
//
//        float[] desirePred = new float[DESIRE_PRED_LEN * DESIRE_LEN];
//        for (int i = 0; i < DESIRE_PRED_LEN; i++) {
//            softmax(model.meta().desirePredProb().get(i).array(), desirePred, i*DESIRE_LEN, DESIRE_LEN);
//        }
//
//        for (int i = 0; i < DISENGAGE_LEN; i++) {
//            gasDisengageProbs.set(i, sigmoid(model.meta().disengageProb().get(i).gasDisengage()));
//            brakeDisengageProbs.set(i, sigmoid(model.meta().disengageProb().get(i).brakeDisengage()));
//            steerOverrideProbs.set(i, sigmoid(model.meta().disengageProb().get(i).steerOverride()));
//            brake3MetersPerSecondSquaredProbs.set(i, sigmoid(model.meta().disengageProb().get(i).brake3ms2()));
//            brake4MetersPerSecondSquaredProbs.set(i, sigmoid(model.meta().disengageProb().get(i).brake4ms2()));
//            brake5MetersPerSecondSquaredProbs.set(i, sigmoid(model.meta().disengageProb().get(i).brake5ms2()));
//        }
//
//        System.arraycopy(prevBrake5ms2Probs, 1, prevBrake5ms2Probs, 0, 4);
//        System.arraycopy(prevBrake3ms2Probs, 1, prevBrake3ms2Probs, 0, 2);
//        prevBrake5ms2Probs[4] = brake5ms2Probs[0];
//        prevBrake3ms2Probs[2] = brake3ms2Probs[0];
//
//        boolean aboveFcwThreshold = true;
//        for (int i = 0; i < prevBrake5ms2Probs.length; i++) {
//            float threshold = i < 2 ? FCW_THRESHOLD_5MS2_LOW : FCW_THRESHOLD_5MS2_HIGH;
//            aboveFcwThreshold = aboveFcwThreshold && prevBrake5ms2Probs[i] > threshold;
//        }
//        for (int i = 0; i < prevBrake3ms2Probs.length; i++) {
//            aboveFcwThreshold = aboveFcwThreshold && prevBrake3ms2Probs[i] > FCW_THRESHOLD_3MS2;
//        }
//
//        meta.setDesirePrediction(desirePred);
//        meta.setDesireState(desireState);
//        meta.setEngagedProb(sigmoid(model.meta().engagedProb()));
//        meta.setHardBrakePredicted(aboveFcwThreshold);
//
//        disengagePredictions.setT(latLongT);
//        disengagePredictions.setGasDisengageProbs(gasDisengageProbs);
//        disengagePredictions.setBrakeDisengageProbs(brakeDisengageProbs);
//        disengagePredictions.setSteerOverrideProbs(steerOverrideProbs);
//        disengagePredictions.setBrake3MetersPerSecondSquaredProbs(brake3ms2Probs);
//        disengagePredictions.setBrake4MetersPerSecondSquaredProbs(brake4ms2Probs);
//        disengagePredictions.setBrake5MetersPerSecondSquaredProbs(brake5ms2Probs);
//
//
//        //Confidence
//        //TODO
//
//        //Leads
//        float[] tOffsets = {0.0f, 2.0f, 4.0f};
//        for (int i = 0; i < LEAD_MHP_SELECTION; i++) {
//            ModelOutput.ModelOutputLeads modelLeads = model.leads();
//            LeadDataV3.Builder leadDataV3 = LeadDataV3.newBuilder();
//
//            float prob = sigmoid(modelLeads.prob().get(i));
//            ModelOutput.LeadPrediction bestLeadPrediction = modelLeads.getBestPrediction(i);
//
//            float[] leadX = new float[LEAD_TRAJ_LEN];
//            float[] leadY = new float[LEAD_TRAJ_LEN];
//            float[] leadV = new float[LEAD_TRAJ_LEN];
//            float[] leadA = new float[LEAD_TRAJ_LEN];
//            float[] leadXStd = new float[LEAD_TRAJ_LEN];
//            float[] leadYStd = new float[LEAD_TRAJ_LEN];
//            float[] leadVStd = new float[LEAD_TRAJ_LEN];
//            float[] leadAStd = new float[LEAD_TRAJ_LEN];
//
//            for (int j = 0; j < LEAD_TRAJ_LEN; j++) {
//                leadX[j] = bestLeadPrediction.mean().get(j).x();
//                leadY[j] = bestLeadPrediction.mean().get(j).y();
//                leadV[j] = bestLeadPrediction.mean().get(j).velocity();
//                leadA[j] = bestLeadPrediction.mean().get(j).acceleration();
//
//                leadXStd[j] = (float) Math.exp(bestLeadPrediction.std().get(j).x());
//                leadYStd[j] = (float) Math.exp(bestLeadPrediction.std().get(j).y());
//
//
//                //Tempoeral pose
//                //TODO
//            }
//        }
//    }
//}
