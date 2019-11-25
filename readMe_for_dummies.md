readMe for dummies

To stat the server simply open your terminal, go to the project directory and...   `sbt run`. That should launch a server, serving in localhost:9000.

After that open your internet browser and go to the localhost:9000.

In the small text box just write the gitHubUrl of the repository of which you want to check the commit history and press the submit button (pressing return will also work)

To go to the next page press next.

///

How it works.

  If the GitHubApi connection doesn't work it will work locally.

  It will basically use this 2 git commands:
    - git clone <repo>
    - git log repo

  The logs fetch will be then parsed to the right Data structured (GitCommitLog).

  Before ending this process the repo will be deleted (since we don't really need it in our computer)

  In the normal scenario, where the GitHubApi doesn't fail, after the user insert the gitHubUrl he wants, and since the the request demands the owner and the repo in order to return the commit history, those are extracted with a simply request and after added to the API GET request.


  TODO

    Wanted to insert a Duration of 5 minutes before considerate it too much time. But could not implement that solution since couldn't find a right way to test it. It wor















