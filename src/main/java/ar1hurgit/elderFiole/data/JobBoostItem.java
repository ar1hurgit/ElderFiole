package ar1hurgit.elderFiole.data;

public class JobBoostItem {
    private final String jobName;
    private final double multiplier;
    private final int durationMinutes;
    private final boolean permanent;
    private int remainingTime; // En secondes
    private boolean active;

    public JobBoostItem(String jobName, double multiplier, int durationMinutes, boolean permanent) {
        this.jobName = jobName;
        this.multiplier = multiplier;
        this.durationMinutes = durationMinutes;
        this.permanent = permanent;
        this.remainingTime = permanent ? -1 : durationMinutes * 60;
        this.active = false;
    }

    public String getJobName() {
        return jobName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void decrementTime(int seconds) {
        if (!permanent && remainingTime > 0) {
            remainingTime -= seconds;
            if (remainingTime < 0) {
                remainingTime = 0;
            }
        }
    }

    public boolean isExpired() {
        return !permanent && remainingTime <= 0;
    }

    public String getFormattedRemainingTime() {
        if (permanent) {
            return "Permanent";
        }

        int hours = remainingTime / 3600;
        int minutes = (remainingTime % 3600) / 60;
        int seconds = remainingTime % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
