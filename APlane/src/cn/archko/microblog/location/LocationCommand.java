package cn.archko.microblog.location;

/**
 * @author: archko 2014/12/4 :16:30
 */
public class LocationCommand implements Command {

    boolean onlyOnce=true;
    boolean openGps=true;
    int scanSpan=1;
    AbsReceiver mAbsReceiver;

    public LocationCommand(AbsReceiver absReceiver) {
        this.mAbsReceiver=absReceiver;
    }

    public boolean isOnlyOnce() {
        return onlyOnce;
    }

    public void setOnlyOnce(boolean onlyOnce) {
        this.onlyOnce=onlyOnce;
    }

    public boolean isOpenGps() {
        return openGps;
    }

    public void setOpenGps(boolean openGps) {
        this.openGps=openGps;
    }

    public int getScanSpan() {
        return scanSpan;
    }

    public void setScanSpan(int scanSpan) {
        this.scanSpan=scanSpan;
    }

    @Override
    public void execute() {
        if (null!=mAbsReceiver) {
            mAbsReceiver.action(this);
        }
    }
}
