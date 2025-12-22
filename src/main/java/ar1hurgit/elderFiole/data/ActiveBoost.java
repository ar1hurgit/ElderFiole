package ar1hurgit.elderFiole.data;

public class ActiveBoost {
    private final String jobName;
    private final double multiplier;
    private final long expirationTime;

    public ActiveBoost(String jobName, double multiplier, long expirationTime) {
        this.jobName = jobName;
        this.multiplier = multiplier;
        this.expirationTime = expirationTime;
    }

    public String getJobName() {
        return jobName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }
}
