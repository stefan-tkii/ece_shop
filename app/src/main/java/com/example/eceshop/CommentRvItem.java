package com.example.eceshop;

import java.util.Date;

public class CommentRvItem
{
    private String userName;
    private String userEmail;
    private String content;
    private String postedAt;

    public CommentRvItem()
    {

    }

    public CommentRvItem(String userName, String userEmail, String content, String postedAt)
    {
        this.userName = userName;
        this.userEmail = userEmail;
        this.content = content;
        this.postedAt = postedAt;
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

}
