package se233.finalcontra.model;

public class Vector2D {
    double x, y;
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D normalize() {
        double length = (double) Math.sqrt(x * x + y * y);
        if (length > 0) {
            return new Vector2D(x / length, y / length);
        }
        return new Vector2D(0, 0);
    }

    public double distance(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float getLength() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
