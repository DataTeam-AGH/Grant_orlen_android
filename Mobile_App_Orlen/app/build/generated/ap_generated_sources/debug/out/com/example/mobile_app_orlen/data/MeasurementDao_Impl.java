package com.example.mobile_app_orlen.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MeasurementDao_Impl implements MeasurementDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Measurement> __insertionAdapterOfMeasurement;

  public MeasurementDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMeasurement = new EntityInsertionAdapter<Measurement>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `measurements` (`id`,`userId`,`ch4Value`,`timestamp`,`isAnomaly`,`notes`,`latitude`,`longitude`,`locationName`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final Measurement entity) {
        statement.bindLong(1, entity.id);
        if (entity.userId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.userId);
        }
        statement.bindDouble(3, entity.ch4Value);
        statement.bindLong(4, entity.timestamp);
        final int _tmp = entity.isAnomaly ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.notes == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.notes);
        }
        statement.bindDouble(7, entity.latitude);
        statement.bindDouble(8, entity.longitude);
        if (entity.locationName == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.locationName);
        }
      }
    };
  }

  @Override
  public void insert(final Measurement measurement) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfMeasurement.insert(measurement);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<Measurement>> getMeasurementsForUser(final String userId) {
    final String _sql = "SELECT * FROM measurements WHERE userId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"measurements"}, false, new Callable<List<Measurement>>() {
      @Override
      @Nullable
      public List<Measurement> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCh4Value = CursorUtil.getColumnIndexOrThrow(_cursor, "ch4Value");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsAnomaly = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomaly");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
          final List<Measurement> _result = new ArrayList<Measurement>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Measurement _item;
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final double _tmpCh4Value;
            _tmpCh4Value = _cursor.getDouble(_cursorIndexOfCh4Value);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsAnomaly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAnomaly);
            _tmpIsAnomaly = _tmp != 0;
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpLocationName;
            if (_cursor.isNull(_cursorIndexOfLocationName)) {
              _tmpLocationName = null;
            } else {
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
            }
            _item = new Measurement(_tmpUserId,_tmpCh4Value,_tmpTimestamp,_tmpIsAnomaly,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpLocationName);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Measurement>> getAllMeasurements() {
    final String _sql = "SELECT * FROM measurements ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"measurements"}, false, new Callable<List<Measurement>>() {
      @Override
      @Nullable
      public List<Measurement> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCh4Value = CursorUtil.getColumnIndexOrThrow(_cursor, "ch4Value");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsAnomaly = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomaly");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
          final List<Measurement> _result = new ArrayList<Measurement>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Measurement _item;
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final double _tmpCh4Value;
            _tmpCh4Value = _cursor.getDouble(_cursorIndexOfCh4Value);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsAnomaly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAnomaly);
            _tmpIsAnomaly = _tmp != 0;
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpLocationName;
            if (_cursor.isNull(_cursorIndexOfLocationName)) {
              _tmpLocationName = null;
            } else {
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
            }
            _item = new Measurement(_tmpUserId,_tmpCh4Value,_tmpTimestamp,_tmpIsAnomaly,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpLocationName);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<Measurement> getMeasurementsForUserSync(final String userId) {
    final String _sql = "SELECT * FROM measurements WHERE userId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfCh4Value = CursorUtil.getColumnIndexOrThrow(_cursor, "ch4Value");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfIsAnomaly = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomaly");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
      final List<Measurement> _result = new ArrayList<Measurement>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Measurement _item;
        final String _tmpUserId;
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _tmpUserId = null;
        } else {
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        }
        final double _tmpCh4Value;
        _tmpCh4Value = _cursor.getDouble(_cursorIndexOfCh4Value);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final boolean _tmpIsAnomaly;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnomaly);
        _tmpIsAnomaly = _tmp != 0;
        final String _tmpNotes;
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _tmpNotes = null;
        } else {
          _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final String _tmpLocationName;
        if (_cursor.isNull(_cursorIndexOfLocationName)) {
          _tmpLocationName = null;
        } else {
          _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
        }
        _item = new Measurement(_tmpUserId,_tmpCh4Value,_tmpTimestamp,_tmpIsAnomaly,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpLocationName);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<Measurement>> getLatestMeasurements() {
    final String _sql = "SELECT * FROM measurements ORDER BY timestamp DESC LIMIT 3";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"measurements"}, false, new Callable<List<Measurement>>() {
      @Override
      @Nullable
      public List<Measurement> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCh4Value = CursorUtil.getColumnIndexOrThrow(_cursor, "ch4Value");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsAnomaly = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomaly");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfLocationName = CursorUtil.getColumnIndexOrThrow(_cursor, "locationName");
          final List<Measurement> _result = new ArrayList<Measurement>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Measurement _item;
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final double _tmpCh4Value;
            _tmpCh4Value = _cursor.getDouble(_cursorIndexOfCh4Value);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsAnomaly;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAnomaly);
            _tmpIsAnomaly = _tmp != 0;
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpLocationName;
            if (_cursor.isNull(_cursorIndexOfLocationName)) {
              _tmpLocationName = null;
            } else {
              _tmpLocationName = _cursor.getString(_cursorIndexOfLocationName);
            }
            _item = new Measurement(_tmpUserId,_tmpCh4Value,_tmpTimestamp,_tmpIsAnomaly,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpLocationName);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getMeasurementsCount() {
    final String _sql = "SELECT COUNT(*) FROM measurements";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"measurements"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getAnomalyCount() {
    final String _sql = "SELECT COUNT(*) FROM measurements WHERE isAnomaly = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"measurements"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
