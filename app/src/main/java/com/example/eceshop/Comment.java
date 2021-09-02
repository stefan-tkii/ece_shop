package com.example.eceshop;

import java.util.Date;

public class Comment
{
    private String userId;
    private String content;
    private long postedAt;

    public Comment()
    {

    }

    public Comment(String userId, String content, long postedAt)
    {
        this.userId = userId;
        this.content = content;
        this.postedAt = postedAt;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public long getPostedAt()
    {
        return postedAt;
    }

    public void setPostedAt(long postedAt)
    {
        this.postedAt = postedAt;
    }

}
