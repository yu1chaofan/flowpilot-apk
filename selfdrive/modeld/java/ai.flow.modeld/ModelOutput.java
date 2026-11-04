// This is a generated file! Please edit source .ksy file and use kaitai-struct-compiler to rebuild

package ai.flow.modeld;

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStruct;
import io.kaitai.struct.KaitaiStream;
import java.io.IOException;
import java.util.ArrayList;

public class ModelOutput extends KaitaiStruct {
    public static ModelOutput fromFile(String fileName) throws IOException {
        return new ModelOutput(new ByteBufferKaitaiStream(fileName));
    }

    public ModelOutput(KaitaiStream _io) {
        this(_io, null, null);
    }

    public ModelOutput(KaitaiStream _io, KaitaiStruct _parent) {
        this(_io, _parent, null);
    }

    public ModelOutput(KaitaiStream _io, KaitaiStruct _parent, ModelOutput _root) {
        super(_io);
        this._parent = _parent;
        this._root = _root == null ? this : _root;
        _read();
    }
    private void _read() {
        this.plans = new ModelOutputPlans(this._io, this, _root);
        this.laneLines = new ModelOutputLaneLines(this._io, this, _root);
        this.roadEdges = new ModelOutputRoadEdges(this._io, this, _root);
        this.leads = new ModelOutputLeads(this._io, this, _root);
        this.meta = new ModelOutputMeta(this._io, this, _root);
        this.pose = new ModelOutputPose(this._io, this, _root);
        this.wideFromDeviceEuler = new ModelOutputWideFromDeviceEuler(this._io, this, _root);
        this.temporalPose = new ModelOutputTemporalPose(this._io, this, _root);
        this.roadTransform = new ModelOutputRoadTransform(this._io, this, _root);
        this.action = new LateralAction(this._io, this, _root);
    }
    public static class ModelOutputLeadPrediction extends KaitaiStruct {
        public static ModelOutputLeadPrediction fromFile(String fileName) throws IOException {
            return new ModelOutputLeadPrediction(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLeadPrediction(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLeadPrediction(KaitaiStream _io, ModelOutput.ModelOutputLeads _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLeadPrediction(KaitaiStream _io, ModelOutput.ModelOutputLeads _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.mean = new ArrayList<ModelOutputLeadElement>();
            for (int i = 0; i < _root().leadTrajLen(); i++) {
                this.mean.add(new ModelOutputLeadElement(this._io, this, _root));
            }
            this.std = new ArrayList<ModelOutputLeadElement>();
            for (int i = 0; i < _root().leadTrajLen(); i++) {
                this.std.add(new ModelOutputLeadElement(this._io, this, _root));
            }
            this.prob = new ArrayList<Float>();
            for (int i = 0; i < _root().leadMhpSelection(); i++) {
                this.prob.add(this._io.readF4le());
            }
        }
        private ArrayList<ModelOutputLeadElement> mean;
        private ArrayList<ModelOutputLeadElement> std;
        private ArrayList<Float> prob;
        private ModelOutput _root;
        private ModelOutput.ModelOutputLeads _parent;
        public ArrayList<ModelOutputLeadElement> mean() { return mean; }
        public ArrayList<ModelOutputLeadElement> std() { return std; }
        public ArrayList<Float> prob() { return prob; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputLeads _parent() { return _parent; }
    }
    public static class ModelOutputRoadEdges extends KaitaiStruct {
        public static ModelOutputRoadEdges fromFile(String fileName) throws IOException {
            return new ModelOutputRoadEdges(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputRoadEdges(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputRoadEdges(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputRoadEdges(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.mean = new ModelOutputEdgessXy(this._io, this, _root);
            this.std = new ModelOutputEdgessXy(this._io, this, _root);
        }
        private ModelOutputEdgessXy mean;
        private ModelOutputEdgessXy std;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputEdgessXy mean() { return mean; }
        public ModelOutputEdgessXy std() { return std; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputLaneLines extends KaitaiStruct {
        public static ModelOutputLaneLines fromFile(String fileName) throws IOException {
            return new ModelOutputLaneLines(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLaneLines(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLaneLines(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLaneLines(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.mean = new ModelOutputLinesXy(this._io, this, _root);
            this.std = new ModelOutputLinesXy(this._io, this, _root);
            this.prob = new ModelOutputLinesProb(this._io, this, _root);
        }
        private ModelOutputLinesXy mean;
        private ModelOutputLinesXy std;
        private ModelOutputLinesProb prob;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputLinesXy mean() { return mean; }
        public ModelOutputLinesXy std() { return std; }
        public ModelOutputLinesProb prob() { return prob; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputPlans extends KaitaiStruct {
        public static ModelOutputPlans fromFile(String fileName) throws IOException {
            return new ModelOutputPlans(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputPlans(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputPlans(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputPlans(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.prediction = new ArrayList<ModelOutputPlanPrediction>();
            for (int i = 0; i < _root().planMhpN(); i++) {
                this.prediction.add(new ModelOutputPlanPrediction(this._io, this, _root));
            }
        }
        private ArrayList<ModelOutputPlanPrediction> prediction;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ArrayList<ModelOutputPlanPrediction> prediction() { return prediction; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputPlanElement extends KaitaiStruct {
        public static ModelOutputPlanElement fromFile(String fileName) throws IOException {
            return new ModelOutputPlanElement(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputPlanElement(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputPlanElement(KaitaiStream _io, ModelOutput.ModelOutputPlanPrediction _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputPlanElement(KaitaiStream _io, ModelOutput.ModelOutputPlanPrediction _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.position = new ModelOutputXyz(this._io, this, _root);
            this.velocity = new ModelOutputXyz(this._io, this, _root);
            this.acceleration = new ModelOutputXyz(this._io, this, _root);
            this.rotation = new ModelOutputXyz(this._io, this, _root);
            this.rotationRate = new ModelOutputXyz(this._io, this, _root);
        }
        private ModelOutputXyz position;
        private ModelOutputXyz velocity;
        private ModelOutputXyz acceleration;
        private ModelOutputXyz rotation;
        private ModelOutputXyz rotationRate;
        private ModelOutput _root;
        private ModelOutput.ModelOutputPlanPrediction _parent;
        public ModelOutputXyz position() { return position; }
        public ModelOutputXyz velocity() { return velocity; }
        public ModelOutputXyz acceleration() { return acceleration; }
        public ModelOutputXyz rotation() { return rotation; }
        public ModelOutputXyz rotationRate() { return rotationRate; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputPlanPrediction _parent() { return _parent; }
    }
    public static class ModelOutputXyz extends KaitaiStruct {
        public static ModelOutputXyz fromFile(String fileName) throws IOException {
            return new ModelOutputXyz(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputXyz(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputXyz(KaitaiStream _io, KaitaiStruct _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputXyz(KaitaiStream _io, KaitaiStruct _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.x = this._io.readF4le();
            this.y = this._io.readF4le();
            this.z = this._io.readF4le();
        }
        private float x;
        private float y;
        private float z;
        private ModelOutput _root;
        private KaitaiStruct _parent;
        public float x() { return x; }
        public float y() { return y; }
        public float z() { return z; }
        public ModelOutput _root() { return _root; }
        public KaitaiStruct _parent() { return _parent; }
    }
    public static class ModelOutputPlanPrediction extends KaitaiStruct {
        public static ModelOutputPlanPrediction fromFile(String fileName) throws IOException {
            return new ModelOutputPlanPrediction(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputPlanPrediction(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputPlanPrediction(KaitaiStream _io, ModelOutput.ModelOutputPlans _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputPlanPrediction(KaitaiStream _io, ModelOutput.ModelOutputPlans _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.mean = new ArrayList<ModelOutputPlanElement>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.mean.add(new ModelOutputPlanElement(this._io, this, _root));
            }
            this.std = new ArrayList<ModelOutputPlanElement>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.std.add(new ModelOutputPlanElement(this._io, this, _root));
            }
            this.prob = this._io.readF4le();
        }
        private ArrayList<ModelOutputPlanElement> mean;
        private ArrayList<ModelOutputPlanElement> std;
        private float prob;
        private ModelOutput _root;
        private ModelOutput.ModelOutputPlans _parent;
        public ArrayList<ModelOutputPlanElement> mean() { return mean; }
        public ArrayList<ModelOutputPlanElement> std() { return std; }
        public float prob() { return prob; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputPlans _parent() { return _parent; }
    }
    public static class ModelOutputLeadElement extends KaitaiStruct {
        public static ModelOutputLeadElement fromFile(String fileName) throws IOException {
            return new ModelOutputLeadElement(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLeadElement(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLeadElement(KaitaiStream _io, ModelOutput.ModelOutputLeadPrediction _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLeadElement(KaitaiStream _io, ModelOutput.ModelOutputLeadPrediction _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.x = this._io.readF4le();
            this.y = this._io.readF4le();
            this.velocity = this._io.readF4le();
            this.acceleration = this._io.readF4le();
        }
        private float x;
        private float y;
        private float velocity;
        private float acceleration;
        private ModelOutput _root;
        private ModelOutput.ModelOutputLeadPrediction _parent;
        public float x() { return x; }
        public float y() { return y; }
        public float velocity() { return velocity; }
        public float acceleration() { return acceleration; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputLeadPrediction _parent() { return _parent; }
    }
    public static class ModelOutputTemporalPose extends KaitaiStruct {
        public static ModelOutputTemporalPose fromFile(String fileName) throws IOException {
            return new ModelOutputTemporalPose(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputTemporalPose(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputTemporalPose(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputTemporalPose(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.velocityMean = new ModelOutputXyz(this._io, this, _root);
            this.rotationMean = new ModelOutputXyz(this._io, this, _root);
            this.velocityStd = new ModelOutputXyz(this._io, this, _root);
            this.rotationStd = new ModelOutputXyz(this._io, this, _root);
        }
        private ModelOutputXyz velocityMean;
        private ModelOutputXyz rotationMean;
        private ModelOutputXyz velocityStd;
        private ModelOutputXyz rotationStd;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputXyz velocityMean() { return velocityMean; }
        public ModelOutputXyz rotationMean() { return rotationMean; }
        public ModelOutputXyz velocityStd() { return velocityStd; }
        public ModelOutputXyz rotationStd() { return rotationStd; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputDisengageProb extends KaitaiStruct {
        public static ModelOutputDisengageProb fromFile(String fileName) throws IOException {
            return new ModelOutputDisengageProb(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputDisengageProb(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputDisengageProb(KaitaiStream _io, ModelOutput.ModelOutputMeta _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputDisengageProb(KaitaiStream _io, ModelOutput.ModelOutputMeta _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.gasDisengage = this._io.readF4le();
            this.brakeDisengage = this._io.readF4le();
            this.steerOverride = this._io.readF4le();
            this.brake3ms2 = this._io.readF4le();
            this.brake4ms2 = this._io.readF4le();
            this.brake5ms2 = this._io.readF4le();
            this.gasPressed = this._io.readF4le();
        }
        private float gasDisengage;
        private float brakeDisengage;
        private float steerOverride;
        private float brake3ms2;
        private float brake4ms2;
        private float brake5ms2;
        private float gasPressed;
        private ModelOutput _root;
        private ModelOutput.ModelOutputMeta _parent;
        public float gasDisengage() { return gasDisengage; }
        public float brakeDisengage() { return brakeDisengage; }
        public float steerOverride() { return steerOverride; }
        public float brake3ms2() { return brake3ms2; }
        public float brake4ms2() { return brake4ms2; }
        public float brake5ms2() { return brake5ms2; }
        public float gasPressed() { return gasPressed; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputMeta _parent() { return _parent; }
    }
    public static class ModelOutputBlinkerProb extends KaitaiStruct {
        public static ModelOutputBlinkerProb fromFile(String fileName) throws IOException {
            return new ModelOutputBlinkerProb(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputBlinkerProb(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputBlinkerProb(KaitaiStream _io, ModelOutput.ModelOutputMeta _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputBlinkerProb(KaitaiStream _io, ModelOutput.ModelOutputMeta _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.left = this._io.readF4le();
            this.right = this._io.readF4le();
        }
        private float left;
        private float right;
        private ModelOutput _root;
        private ModelOutput.ModelOutputMeta _parent;
        public float left() { return left; }
        public float right() { return right; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputMeta _parent() { return _parent; }
    }
    public static class ModelOutputLineProbVal extends KaitaiStruct {
        public static ModelOutputLineProbVal fromFile(String fileName) throws IOException {
            return new ModelOutputLineProbVal(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLineProbVal(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLineProbVal(KaitaiStream _io, ModelOutput.ModelOutputLinesProb _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLineProbVal(KaitaiStream _io, ModelOutput.ModelOutputLinesProb _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.valDeprecated = this._io.readF4le();
            this.val = this._io.readF4le();
        }
        private float valDeprecated;
        private float val;
        private ModelOutput _root;
        private ModelOutput.ModelOutputLinesProb _parent;
        public float valDeprecated() { return valDeprecated; }
        public float val() { return val; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputLinesProb _parent() { return _parent; }
    }
    public static class ModelOutputWideFromDeviceEuler extends KaitaiStruct {
        public static ModelOutputWideFromDeviceEuler fromFile(String fileName) throws IOException {
            return new ModelOutputWideFromDeviceEuler(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputWideFromDeviceEuler(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputWideFromDeviceEuler(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputWideFromDeviceEuler(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.mean = new ModelOutputXyz(this._io, this, _root);
            this.std = new ModelOutputXyz(this._io, this, _root);
        }
        private ModelOutputXyz mean;
        private ModelOutputXyz std;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputXyz mean() { return mean; }
        public ModelOutputXyz std() { return std; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputLeads extends KaitaiStruct {
        public static ModelOutputLeads fromFile(String fileName) throws IOException {
            return new ModelOutputLeads(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLeads(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLeads(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLeads(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.prediction = new ArrayList<ModelOutputLeadPrediction>();
            for (int i = 0; i < _root().leadMhpN(); i++) {
                this.prediction.add(new ModelOutputLeadPrediction(this._io, this, _root));
            }
            this.prob = new ArrayList<Float>();
            for (int i = 0; i < _root().leadMhpSelection(); i++) {
                this.prob.add(this._io.readF4le());
            }
        }
        private ArrayList<ModelOutputLeadPrediction> prediction;
        private ArrayList<Float> prob;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ArrayList<ModelOutputLeadPrediction> prediction() { return prediction; }
        public ArrayList<Float> prob() { return prob; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputEdgessXy extends KaitaiStruct {
        public static ModelOutputEdgessXy fromFile(String fileName) throws IOException {
            return new ModelOutputEdgessXy(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputEdgessXy(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputEdgessXy(KaitaiStream _io, ModelOutput.ModelOutputRoadEdges _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputEdgessXy(KaitaiStream _io, ModelOutput.ModelOutputRoadEdges _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.left = new ArrayList<ModelOutputYz>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.left.add(new ModelOutputYz(this._io, this, _root));
            }
            this.right = new ArrayList<ModelOutputYz>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.right.add(new ModelOutputYz(this._io, this, _root));
            }
        }
        private ArrayList<ModelOutputYz> left;
        private ArrayList<ModelOutputYz> right;
        private ModelOutput _root;
        private ModelOutput.ModelOutputRoadEdges _parent;
        public ArrayList<ModelOutputYz> left() { return left; }
        public ArrayList<ModelOutputYz> right() { return right; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputRoadEdges _parent() { return _parent; }
    }
    public static class ModelOutputPose extends KaitaiStruct {
        public static ModelOutputPose fromFile(String fileName) throws IOException {
            return new ModelOutputPose(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputPose(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputPose(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputPose(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.velocityMean = new ModelOutputXyz(this._io, this, _root);
            this.rotationMean = new ModelOutputXyz(this._io, this, _root);
            this.velocityStd = new ModelOutputXyz(this._io, this, _root);
            this.rotationStd = new ModelOutputXyz(this._io, this, _root);
        }
        private ModelOutputXyz velocityMean;
        private ModelOutputXyz rotationMean;
        private ModelOutputXyz velocityStd;
        private ModelOutputXyz rotationStd;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputXyz velocityMean() { return velocityMean; }
        public ModelOutputXyz rotationMean() { return rotationMean; }
        public ModelOutputXyz velocityStd() { return velocityStd; }
        public ModelOutputXyz rotationStd() { return rotationStd; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class LateralAction extends KaitaiStruct {
        public static LateralAction fromFile(String fileName) throws IOException {
            return new LateralAction(new ByteBufferKaitaiStream(fileName));
        }

        public LateralAction(KaitaiStream _io) {
            this(_io, null, null);
        }

        public LateralAction(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public LateralAction(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.desiredCurvatures = this._io.readF4le();
        }
        private float desiredCurvatures;
        private ModelOutput _root;
        private ModelOutput _parent;
        public float desiredCurvatures() { return desiredCurvatures; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputDesireProb extends KaitaiStruct {
        public static ModelOutputDesireProb fromFile(String fileName) throws IOException {
            return new ModelOutputDesireProb(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputDesireProb(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputDesireProb(KaitaiStream _io, ModelOutput.ModelOutputMeta _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputDesireProb(KaitaiStream _io, ModelOutput.ModelOutputMeta _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.noneValue = this._io.readF4le();
            this.turnLeft = this._io.readF4le();
            this.turnRight = this._io.readF4le();
            this.laneChangeLeft = this._io.readF4le();
            this.laneChangeRight = this._io.readF4le();
            this.keepLeft = this._io.readF4le();
            this.keepRight = this._io.readF4le();
            this.nullValue = this._io.readF4le();
        }
        private ArrayList<Float> raw;
        public ArrayList<Float> raw() {
            if (this.raw != null)
                return this.raw;
            long _pos = this._io.pos();
            this._io.seek(0);
            this.raw = new ArrayList<Float>();
            for (int i = 0; i < 8; i++) {
                this.raw.add(this._io.readF4le());
            }
            this._io.seek(_pos);
            return this.raw;
        }
        private float noneValue;
        private float turnLeft;
        private float turnRight;
        private float laneChangeLeft;
        private float laneChangeRight;
        private float keepLeft;
        private float keepRight;
        private float nullValue;
        private ModelOutput _root;
        private ModelOutput.ModelOutputMeta _parent;
        public float noneValue() { return noneValue; }
        public float turnLeft() { return turnLeft; }
        public float turnRight() { return turnRight; }
        public float laneChangeLeft() { return laneChangeLeft; }
        public float laneChangeRight() { return laneChangeRight; }
        public float keepLeft() { return keepLeft; }
        public float keepRight() { return keepRight; }
        public float nullValue() { return nullValue; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputMeta _parent() { return _parent; }
    }
    public static class ModelOutputMeta extends KaitaiStruct {
        public static ModelOutputMeta fromFile(String fileName) throws IOException {
            return new ModelOutputMeta(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputMeta(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputMeta(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputMeta(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.desireStateProb = new ModelOutputDesireProb(this._io, this, _root);
            this.engagedProb = this._io.readF4le();
            this.disengageProb = new ArrayList<ModelOutputDisengageProb>();
            for (int i = 0; i < _root().disengageLen(); i++) {
                this.disengageProb.add(new ModelOutputDisengageProb(this._io, this, _root));
            }
            this.blinkerProb = new ArrayList<ModelOutputBlinkerProb>();
            for (int i = 0; i < _root().blinkerLen(); i++) {
                this.blinkerProb.add(new ModelOutputBlinkerProb(this._io, this, _root));
            }
            this.desirePredProb = new ArrayList<ModelOutputDesireProb>();
            for (int i = 0; i < _root().desirePredLen(); i++) {
                this.desirePredProb.add(new ModelOutputDesireProb(this._io, this, _root));
            }
        }
        private ModelOutputDesireProb desireStateProb;
        private float engagedProb;
        private ArrayList<ModelOutputDisengageProb> disengageProb;
        private ArrayList<ModelOutputBlinkerProb> blinkerProb;
        private ArrayList<ModelOutputDesireProb> desirePredProb;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputDesireProb desireStateProb() { return desireStateProb; }
        public float engagedProb() { return engagedProb; }
        public ArrayList<ModelOutputDisengageProb> disengageProb() { return disengageProb; }
        public ArrayList<ModelOutputBlinkerProb> blinkerProb() { return blinkerProb; }
        public ArrayList<ModelOutputDesireProb> desirePredProb() { return desirePredProb; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputYz extends KaitaiStruct {
        public static ModelOutputYz fromFile(String fileName) throws IOException {
            return new ModelOutputYz(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputYz(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputYz(KaitaiStream _io, KaitaiStruct _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputYz(KaitaiStream _io, KaitaiStruct _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.y = this._io.readF4le();
            this.z = this._io.readF4le();
        }
        private float y;
        private float z;
        private ModelOutput _root;
        private KaitaiStruct _parent;
        public float y() { return y; }
        public float z() { return z; }
        public ModelOutput _root() { return _root; }
        public KaitaiStruct _parent() { return _parent; }
    }
    public static class ModelOutputRoadTransform extends KaitaiStruct {
        public static ModelOutputRoadTransform fromFile(String fileName) throws IOException {
            return new ModelOutputRoadTransform(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputRoadTransform(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputRoadTransform(KaitaiStream _io, ModelOutput _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputRoadTransform(KaitaiStream _io, ModelOutput _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.positionMean = new ModelOutputXyz(this._io, this, _root);
            this.rotationMean = new ModelOutputXyz(this._io, this, _root);
            this.positionStd = new ModelOutputXyz(this._io, this, _root);
            this.rotationStd = new ModelOutputXyz(this._io, this, _root);
        }
        private ModelOutputXyz positionMean;
        private ModelOutputXyz rotationMean;
        private ModelOutputXyz positionStd;
        private ModelOutputXyz rotationStd;
        private ModelOutput _root;
        private ModelOutput _parent;
        public ModelOutputXyz positionMean() { return positionMean; }
        public ModelOutputXyz rotationMean() { return rotationMean; }
        public ModelOutputXyz positionStd() { return positionStd; }
        public ModelOutputXyz rotationStd() { return rotationStd; }
        public ModelOutput _root() { return _root; }
        public ModelOutput _parent() { return _parent; }
    }
    public static class ModelOutputLinesXy extends KaitaiStruct {
        public static ModelOutputLinesXy fromFile(String fileName) throws IOException {
            return new ModelOutputLinesXy(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLinesXy(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLinesXy(KaitaiStream _io, ModelOutput.ModelOutputLaneLines _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLinesXy(KaitaiStream _io, ModelOutput.ModelOutputLaneLines _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.leftFar = new ArrayList<ModelOutputYz>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.leftFar.add(new ModelOutputYz(this._io, this, _root));
            }
            this.leftNear = new ArrayList<ModelOutputYz>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.leftNear.add(new ModelOutputYz(this._io, this, _root));
            }
            this.rightNear = new ArrayList<ModelOutputYz>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.rightNear.add(new ModelOutputYz(this._io, this, _root));
            }
            this.rightFar = new ArrayList<ModelOutputYz>();
            for (int i = 0; i < _root().trajectorySize(); i++) {
                this.rightFar.add(new ModelOutputYz(this._io, this, _root));
            }
        }
        private ArrayList<ModelOutputYz> leftFar;
        private ArrayList<ModelOutputYz> leftNear;
        private ArrayList<ModelOutputYz> rightNear;
        private ArrayList<ModelOutputYz> rightFar;
        private ModelOutput _root;
        private ModelOutput.ModelOutputLaneLines _parent;
        public ArrayList<ModelOutputYz> leftFar() { return leftFar; }
        public ArrayList<ModelOutputYz> leftNear() { return leftNear; }
        public ArrayList<ModelOutputYz> rightNear() { return rightNear; }
        public ArrayList<ModelOutputYz> rightFar() { return rightFar; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputLaneLines _parent() { return _parent; }
    }
    public static class ModelOutputLinesProb extends KaitaiStruct {
        public static ModelOutputLinesProb fromFile(String fileName) throws IOException {
            return new ModelOutputLinesProb(new ByteBufferKaitaiStream(fileName));
        }

        public ModelOutputLinesProb(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ModelOutputLinesProb(KaitaiStream _io, ModelOutput.ModelOutputLaneLines _parent) {
            this(_io, _parent, null);
        }

        public ModelOutputLinesProb(KaitaiStream _io, ModelOutput.ModelOutputLaneLines _parent, ModelOutput _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.leftFar = new ModelOutputLineProbVal(this._io, this, _root);
            this.leftNear = new ModelOutputLineProbVal(this._io, this, _root);
            this.rightNear = new ModelOutputLineProbVal(this._io, this, _root);
            this.rightFar = new ModelOutputLineProbVal(this._io, this, _root);
        }
        private ModelOutputLineProbVal leftFar;
        private ModelOutputLineProbVal leftNear;
        private ModelOutputLineProbVal rightNear;
        private ModelOutputLineProbVal rightFar;
        private ModelOutput _root;
        private ModelOutput.ModelOutputLaneLines _parent;
        public ModelOutputLineProbVal leftFar() { return leftFar; }
        public ModelOutputLineProbVal leftNear() { return leftNear; }
        public ModelOutputLineProbVal rightNear() { return rightNear; }
        public ModelOutputLineProbVal rightFar() { return rightFar; }
        public ModelOutput _root() { return _root; }
        public ModelOutput.ModelOutputLaneLines _parent() { return _parent; }
    }
    private Byte desirePredLen;
    public Byte desirePredLen() {
        if (this.desirePredLen != null)
            return this.desirePredLen;
        byte _tmp = (byte) (4);
        this.desirePredLen = _tmp;
        return this.desirePredLen;
    }
    private Byte blinkerLen;
    public Byte blinkerLen() {
        if (this.blinkerLen != null)
            return this.blinkerLen;
        byte _tmp = (byte) (6);
        this.blinkerLen = _tmp;
        return this.blinkerLen;
    }
    private Byte leadMhpN;
    public Byte leadMhpN() {
        if (this.leadMhpN != null)
            return this.leadMhpN;
        byte _tmp = (byte) (2);
        this.leadMhpN = _tmp;
        return this.leadMhpN;
    }
    private Byte trajectorySize;
    public Byte trajectorySize() {
        if (this.trajectorySize != null)
            return this.trajectorySize;
        byte _tmp = (byte) (33);
        this.trajectorySize = _tmp;
        return this.trajectorySize;
    }
    private Byte disengageLen;
    public Byte disengageLen() {
        if (this.disengageLen != null)
            return this.disengageLen;
        byte _tmp = (byte) (5);
        this.disengageLen = _tmp;
        return this.disengageLen;
    }
    private Byte planMhpN;
    public Byte planMhpN() {
        if (this.planMhpN != null)
            return this.planMhpN;
        byte _tmp = (byte) (5);
        this.planMhpN = _tmp;
        return this.planMhpN;
    }
    private Byte leadMhpSelection;
    public Byte leadMhpSelection() {
        if (this.leadMhpSelection != null)
            return this.leadMhpSelection;
        byte _tmp = (byte) (3);
        this.leadMhpSelection = _tmp;
        return this.leadMhpSelection;
    }
    private Byte leadTrajLen;
    public Byte leadTrajLen() {
        if (this.leadTrajLen != null)
            return this.leadTrajLen;
        byte _tmp = (byte) (6);
        this.leadTrajLen = _tmp;
        return this.leadTrajLen;
    }
    private ModelOutputPlans plans;
    private ModelOutputLaneLines laneLines;
    private ModelOutputRoadEdges roadEdges;
    private ModelOutputLeads leads;
    private ModelOutputMeta meta;
    private ModelOutputPose pose;
    private ModelOutputWideFromDeviceEuler wideFromDeviceEuler;
    private ModelOutputTemporalPose temporalPose;
    private ModelOutputRoadTransform roadTransform;
    private LateralAction action;
    private ModelOutput _root;
    private KaitaiStruct _parent;
    public ModelOutputPlans plans() { return plans; }
    public ModelOutputLaneLines laneLines() { return laneLines; }
    public ModelOutputRoadEdges roadEdges() { return roadEdges; }
    public ModelOutputLeads leads() { return leads; }
    public ModelOutputMeta meta() { return meta; }
    public ModelOutputPose pose() { return pose; }
    public ModelOutputWideFromDeviceEuler wideFromDeviceEuler() { return wideFromDeviceEuler; }
    public ModelOutputTemporalPose temporalPose() { return temporalPose; }
    public ModelOutputRoadTransform roadTransform() { return roadTransform; }
    public LateralAction action() { return action; }
    public ModelOutput _root() { return _root; }
    public KaitaiStruct _parent() { return _parent; }
}