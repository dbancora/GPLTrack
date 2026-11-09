# GPL Track 🚗⛽

Un'applicazione Android nativa, moderna e dal design **Glassmorphism**, pensata per tenere traccia dei consumi della propria auto (Benzina e GPL), delle spese per i lavaggi e delle scadenze (Bollo, Assicurazione, Patente).

## ✨ Caratteristiche

- **Doppio Carburante (Benzina & GPL):** Calcolo automatico dei consumi (L/100 km) e dei chilometri percorsi basandosi sull'odometro totale dell'auto, aggirando i reset del computer di bordo.
- **Timeline Unificata:** Una cronologia elegante che unisce rifornimenti e lavaggi.
- **Grafico Spese Mensili:** Un grafico interattivo animato che mostra l'andamento delle spese mensili (Benzina, GPL, Lavaggi) realizzato con Canvas nativo.
- **Promemoria Scadenze:** Sistema di notifiche intelligente basato su `WorkManager` per ricordare bollo, assicurazione e patente senza consumare batteria.
- **Design Glassmorphism:** Interfaccia utente interamente personalizzata con effetti di vetro smerigliato (Haze), sfondi aurora animati e transizioni fluide.
- **Backup e Ripristino:** Esportazione e importazione di tutti i dati (rifornimenti, lavaggi e profilo) in un unico file JSON.

## 🛠 Stack Tecnico

- **Linguaggio:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architettura:** MVVM (Model-View-ViewModel)
- **Librerie Principali:**
    - [Haze](https://github.com/chrisbanes/haze) per l'effetto vetro
    - WorkManager per le notifiche in background
    - Jetpack Navigation (gestita via stato)
    - JSON nativo per la serializzazione locale

## 🚀 Come Compilare il Progetto

1. Clona il repository:
   ```bash
   git clone https://github.com/TUO_USERNAME/GPLTrack.git
2. Apri il progetto in Android Studio (versione Koala o superiore).
3. Lascia che Gradle scarichi le dipendenze.
4. Clicca su Run ▶️ per compilare e installare l'app sul tuo emulatore o dispositivo fisico.