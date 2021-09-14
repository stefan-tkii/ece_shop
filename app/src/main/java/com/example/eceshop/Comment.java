package com.example.eceshop;

public class Comment
{
    private String userId;
    private String content;
    private long postedAt;
    private boolean removed;

    public Comment()
    {

    }

    public Comment(String userId, String content, long postedAt, boolean removed)
    {
        this.userId = userId;
        this.content = content;
        this.postedAt = postedAt;
        this.removed = removed;
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

    public boolean isRemoved()
    {
        return removed;
    }

    public void setRemoved(boolean removed)
    {
        this.removed = removed;
    }

}
