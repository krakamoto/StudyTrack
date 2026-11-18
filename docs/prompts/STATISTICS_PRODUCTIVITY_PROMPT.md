# Prompt — Statistiche di produttività e andamento

## Obiettivo
Estendere la schermata Statistics di StudyTrack con indicatori e grafici che aiutino
lo studente a capire non solo *quanto* ha studiato, ma anche *quanto è stato costante*,
*quando rende meglio* e *come sta cambiando il suo andamento nel tempo*.

## Requisiti funzionali
1. Mostrare un **Focus Score** da 0 a 100 calcolato in modo deterministico da:
   - costanza settimanale (giorni attivi su 7);
   - volume di studio rispetto a un obiettivo giornaliero di 2 ore;
   - durata media delle sessioni;
   - streak corrente.
2. Mostrare il **trend degli ultimi 7 giorni** rispetto ai 7 giorni precedenti,
   con percentuale e indicazione positiva/negativa.
3. Mostrare KPI utili: tempo oggi, tempo della settimana, numero di sessioni,
   durata media per sessione, giorni attivi e percentuale di costanza.
4. Aggiungere un **grafico lineare degli ultimi 14 giorni** con:
   - tempo giornaliero;
   - media mobile a 7 giorni;
   - legenda e trend rispetto al periodo precedente.
5. Aggiungere un'analisi **"Quando studi"**:
   - distribuzione del tempo per ora del giorno;
   - ora più produttiva;
   - distribuzione per giorno della settimana;
   - giorno più produttivo.
6. Mantenere i grafici Giornaliero / Settimanale / Mensile e la ripartizione
   per progetto, migliorando la leggibilità.
7. Gestire correttamente le sessioni che attraversano più ore o più giorni,
   distribuendo la durata nelle rispettive fasce.
8. Non introdurre dipendenze da librerie di grafici: usare Canvas e Compose.
9. Aggiungere test unitari per i calcoli principali.

## Requisiti qualitativi
- Architettura pulita: i calcoli devono stare fuori dalla UI e dal ViewModel.
- UI in italiano, coerente con Material 3 e il tema scuro esistente.
- Nessuna modifica distruttiva ai dati esistenti.
- Build, unit test e lint devono passare.
- Verifica manuale su device tramite ADB.

## Criteri di accettazione
- La schermata mostra Focus Score, trend, KPI, grafico 14 giorni e analisi oraria/settimanale.
- I grafici si aggiornano automaticamente quando le sessioni cambiano.
- Le sessioni a cavallo di mezzanotte o di un'ora sono conteggiate correttamente.
- L'APK compila e i test passano.
