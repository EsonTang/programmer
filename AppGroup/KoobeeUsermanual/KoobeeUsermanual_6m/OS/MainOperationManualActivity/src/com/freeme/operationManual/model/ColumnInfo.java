package com.freeme.operationManual.model;

import java.io.Serializable;
import java.util.List;

public class ColumnInfo
  implements Serializable
{
  private static final long serialVersionUID = 1372681933253073420L;
  private String mCurColumnPath;
  private String mCurPageName;
  private List<String> mPageNameList;

  public ColumnInfo()
  {
  }

  public ColumnInfo(String paramString1, String paramString2, List<String> paramList)
  {
    this.mCurPageName = paramString1;
    this.mCurColumnPath = paramString2;
    this.mPageNameList = paramList;
  }

  public String getmCurColumnPath()
  {
    return this.mCurColumnPath;
  }

  public String getmCurPageName()
  {
    return this.mCurPageName;
  }

  public List<String> getmPageNameList()
  {
    return this.mPageNameList;
  }

  public void setmCurColumnPath(String paramString)
  {
    this.mCurColumnPath = paramString;
  }

  public void setmCurPageName(String paramString)
  {
    this.mCurPageName = paramString;
  }

  public void setmPageNameList(List<String> paramList)
  {
    this.mPageNameList = paramList;
  }
}