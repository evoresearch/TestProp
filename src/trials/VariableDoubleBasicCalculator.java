package trials;

public class VariableDoubleBasicCalculator {
    public int add(int a,int b){
        int result = a+b;
        return result;
    }
    public int subtract(int a,int b){
        int result = a>b?a-b:b-a;
        return result;
    }

    public double multiply(int a,int b){
        return a*b;
    }
    public double divide(int a,int b){
        return a/b;
    }
    public double divide(double a,double b){
        return a/b;
    }
}
