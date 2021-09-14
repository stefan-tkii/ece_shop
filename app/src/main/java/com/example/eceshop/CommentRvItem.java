package com.example.eceshop;

public class CommentRvItem
{

    private String id;
    private String userName;
    private String userEmail;
    private String content;
    private String postedAt;
    private boolean removed;

    public CommentRvItem()
    {

    }

    public CommentRvItem(String id, String userName, String userEmail, String content, String postedAt, boolean removed)
    {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.content = content;
        this.postedAt = postedAt;
        this.removed = removed;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getPostedAt()
    {
        return postedAt;
    }

    public void setPostedAt(String postedAt)
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
