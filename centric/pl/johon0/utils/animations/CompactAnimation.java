package centric.pl.johon0.utils.animations;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompactAnimation {

    Easing easing;
    long duration;

    long startTime;
    long millis;
    double startValue;
    double destinationValue;
    double value;
    boolean finished;

    public CompactAnimation(Easing easing, long duration) {
        this.easing = easing;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public void run(double destinationValue) {
        this.millis = System.currentTimeMillis();
        if (this.destinationValue != destinationValue) {
            this.destinationValue = destinationValue;
            this.reset();
        } else {
            this.finished = this.millis - this.duration > this.startTime;
            if (this.finished) {
                this.value = destinationValue;
                return;
            }
        }
        final double result = this.easing.getFunction().apply(this.getProgress());
        if (this.value > destinationValue) {
            this.value = this.startValue - (this.startValue - destinationValue) * result;
        } else this.value = this.startValue + (destinationValue - this.startValue) * result;
    }

    public Number getNumberValue() {
        return getValue();
    }

    public double getProgress() {
        return (double) (System.currentTimeMillis() - this.startTime) / (double) this.duration;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.startValue = value;
        this.finished = false;
    }

}

