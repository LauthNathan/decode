package com.cesi.decode.services;

import javax.ejb.Local;
import java.sql.SQLException;

@Local
public interface DecodeServiceLocal {
    public boolean isFrench(String content) throws SQLException;
    public String searchSecret(String content);
}
