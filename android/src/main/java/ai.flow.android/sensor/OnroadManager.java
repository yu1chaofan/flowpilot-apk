package ai.flow.android.sensor;

import android.content.Context;
import android.content.Intent;

import ai.flow.sensor.SensorInterface;

import ai.flow.openpilot.ServiceCalibrationd;
import ai.flow.openpilot.ServiceControlsd;
import ai.flow.openpilot.ServicePlannerd;
import ai.flow.openpilot.ServiceRadard;
import ai.flow.openpilot.ServiceFlowreset;

public class OnroadManager implements SensorInterface {
    private Context ctx;

    public OnroadManager(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void dispose() {

    }

    public void start() {
        System.out.println("Onroad was called, starting services!");
    }

    public void stop() {
        // Actually, restart, as they spinloop waiting for CarParams
        ServiceFlowreset.start(this.ctx, "");
        this.ctx.stopService(new Intent(this.ctx, ServiceControlsd.class));
        this.ctx.stopService(new Intent(this.ctx, ServiceRadard.class));
		this.ctx.stopService(new Intent(this.ctx, ServiceCalibrationd.class));
		this.ctx.stopService(new Intent(this.ctx, ServicePlannerd.class));

		ServiceControlsd.start(this.ctx, "");
		ServiceRadard.start(this.ctx, "");
		ServiceCalibrationd.start(this.ctx, "");
		ServicePlannerd.start(this.ctx, "");
    }
}