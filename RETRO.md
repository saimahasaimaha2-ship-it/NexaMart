# Sprint Retrospectives

One entry per sprint: what worked, what didn't, one change for the next sprint.

## Sprint 1 (Jul 27 – Aug 9): Auth and core flow
- **Worked:** The layered Servlet -> Service -> DAO structure was set up early, so register/login, browse, cart and checkout slotted in cleanly for the MVP demo.
- **Didn't:** Some setup time was lost getting Tomcat, Maven and the H2 server mode (TCP and web flags) to work together locally.
- **Change:** Write the local run commands down in the README as soon as they work.

## Sprint 2 (Aug 10 – Aug 23): Seller dashboard and admin panel
- **Worked:** Seller dashboard (create / edit / delete with seller-ownership checks in SQL) and the admin panel were tested end-to-end in the browser as they were built.
- **Didn't:** A bug where seller incoming orders showed no items (missing item-loading loop) and a `passwordHash` leak in the admin users response were only caught later during testing and the spec audit.
- **Change:** Check every new JSON response for sensitive fields and for missing joined data before moving on.

## Sprint 3 (Aug 24 – Sep 6): Search, reviews and spec audit
- **Worked:** Auditing the code against the spec found real gaps early: DTO mapping for admin users, `/api/v1/health`, the migrations folder, `config.properties` removed from git, custom error pages.
- **Didn't:** Reviews and ratings (F8, a mandatory feature) slipped about a week, and several days of work had not been committed or pushed, which put the commit-frequency rule at risk.
- **Change:** Commit and push at the end of every work session, and build mandatory features before extras.

## Sprint 4 (Sep 7 – Sep 21): Tests, deployment and documentation
- **Worked:** Constructor-injected DAOs made Mockito service tests easy (35 tests passing), CI went green, and the Docker deployment on Render went live.
- **Didn't:** Render's free tier resets the H2 database on every restart, which first showed up as an empty live site; an ambiguous staff notice about the Sep 21 date also caused an unnecessary rush.
- **Change:** Run the schema on startup, seed demo data, re-check the live site before every review, and confirm dates with the staff notice before re-planning.

## Sprint 5 (Sep 22 – Oct 10): UI polish and final review
- **Worked:** Review-2 feedback about the plain UI led to a purple theme with a shared navbar, product images and a styled landing page without touching backend logic; a load test (10 users, 60 s, 0 errors) and final report were added.
- **Didn't:** Checkstyle/SpotBugs, structured logging and servlet-level tests were left out for lack of time.
- **Change:** For future projects, set up static analysis and request logging in the first sprint rather than at the end.
