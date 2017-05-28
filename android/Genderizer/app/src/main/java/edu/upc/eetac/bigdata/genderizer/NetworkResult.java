package edu.upc.eetac.bigdata.genderizer;

public interface NetworkResult {
    public void onSuccess(Gender gender);
    public void onError();
}
