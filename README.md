# 🌱 HopeGuide AI

A multi-modal, GenAI-powered recovery and prevention platform for individuals navigating substance use disorders and their families.

## 🔗 Links
- **GitHub:** https://github.com/kantheti1306/HopeGuide-AI
- **Frontend (Vercel):** https://hopeguide-ai.vercel.app
- **Backend Health:** https://hopeguide-backend.onrender.com/api/auth/health

## 🧪 Demo Credentials
| Role | Email | Password |
|------|-------|----------|
| Individual | user@hopeguide.com | Demo@123 |
| Family Member | family@hopeguide.com | Demo@123 |

## 🤖 GenAI Used
**Google Gemini 1.5 Flash API** used in:
1. Crisis/Intervention response
2. Support script generation
3. Family guidance
4. Mood log AI analysis
5. Resource topic explanation

## 🛠️ Tech Stack
- **Frontend:** React + Vite + Tailwind CSS
- **Backend:** Spring Boot + JWT + PostgreSQL
- **AI:** Google Gemini 1.5 Flash
- **Deploy:** Vercel (frontend) + Render (backend + DB)

## 🚀 Local Setup

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Env vars
```env
VITE_API_BASE_URL=http://localhost:8080
```

## Render Env Vars
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/DB
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
JWT_SECRET=HopeGuideJwtSecretKeyMustBeAtLeast32CharactersLong2026
GEMINI_API_KEY=your-gemini-key
FRONTEND_URL=https://hopeguide-ai.vercel.app
```
