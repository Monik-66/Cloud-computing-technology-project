# Campusly college portal

Responsive React portal for students, faculty, and directors, with a Windows desktop shell powered by Electron. The UI uses the attendance dashboard’s light/dark direction and connects to the Spring Boot API.

## Run the website

```sh
npm install
npm run dev
```

The API defaults to `https://cloud-computing-technology-project.onrender.com/api/v1`. To use another backend, copy `.env.example` to `.env.local` and set `VITE_API_BASE_URL`.

## Run the desktop app

```sh
npm run desktop
```

Build a Windows installer with `npm run package:win`.

## Portal features

- Sign in through `/auth/login`; the JWT is stored in browser local storage and attached to authenticated requests.
- Student view: attendance summary, subject breakdown, section timetable, and alerts.
- Faculty view: timetable, active and past sessions, QR session launch, live check-in count, and manual attendance by student ID.
- Director view: institution counts, configurable low-attendance report with CSV export, and audit trail.
- Light/dark theme preference persists across sessions.

The API currently returns live session totals but not the checked-in student roster. The low-attendance response also omits student phone numbers and section names, so those fields are not shown. Manual attendance uses student IDs because the API has no roster endpoint for an active session.
