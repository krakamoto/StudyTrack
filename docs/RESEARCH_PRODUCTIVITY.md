# Ricerca su produttività e apprendimento

Questa nota documenta i riferimenti usati per aggiornare il Focus Score e i consigli
mostrati nell'app. La ricerca è stata usata per orientare scelte di prodotto, non per
formulare diagnosi o consigli clinici.

## Fonti consultate

1. **Edwin A. Locke & Gary P. Latham (2002)** — *Building a Practically Useful Theory
   of Goal Setting and Task Motivation*, American Psychologist, 57(9), 705–717.
   DOI: `10.1037/0003-066X.57.9.705`.
   - Obiettivi specifici e sfidanti producono prestazioni migliori di obiettivi vaghi
     come “dare il massimo”.
   - Feedback e impegno verso l'obiettivo sono fondamentali.
   - La sintesi enciclopedica della teoria riporta che la maggior parte degli studi
     esaminati trovava prestazioni superiori con obiettivi specifici e difficili.

2. **Nicholas J. Cepeda et al. (2006)** — *Distributed Practice in Verbal Recall
   Tasks: A Review and Quantitative Synthesis*, Psychological Bulletin, 132(3),
   354–380. DOI: `10.1037/0033-2909.132.3.354`.
   - La pratica distribuita nel tempo produce generalmente un ricordo migliore
     rispetto alla pratica concentrata (cramming).

3. **John Dunlosky et al. (2013)** — *Improving Students' Learning With Effective
   Learning Techniques*, Psychological Science in the Public Interest, 14(1), 4–58.
   DOI: `10.1177/1529100612453266`.
   - La pratica distribuita è tra le tecniche di studio con maggiore utilità stimata.

4. **Francesco Cirillo — The Pomodoro Technique** e letteratura di sintesi
   successiva.
   - Sessioni di lavoro focalizzate di circa 25 minuti, seguite da pause brevi;
     dopo alcuni cicli, una pausa più lunga.
   - Una scoping review del 2025 sulla tecnica Pomodoro riporta miglioramenti
     consistenti di focus e riduzione della fatica mentale rispetto a pause
     auto-regolate. PMID: `41107936`.

## Traduzione nelle funzionalità dell'app

| Evidenza | Implementazione |
|---|---|
| Obiettivi specifici e misurabili | Obiettivo giornaliero configurabile in minuti/ore |
| La quantità conta, non solo la presenza | Completamento obiettivo con credito parziale per le ore studiate |
| La costanza nel tempo è importante | Streak basata sul raggiungimento dell'obiettivo giornaliero |
| La pratica distribuita è più efficace | Insight che incoraggia lo studio su più giorni |
| Sessioni focalizzate e pause | Suggerimento di blocchi da 25–50 minuti con pause brevi |
| Feedback sul progresso | Grafico di completamento obiettivo a 7 giorni e trend settimanale |

## Formula aggiornata del Focus Score

Il Focus Score è una metrica euristica da 0 a 100:

- **50 punti — Completamento obiettivo**
  - Per ogni giorno degli ultimi 7: `min(tempo studiato / obiettivo giornaliero, 1)`.
  - La media dei sette valori viene moltiplicata per 50.
  - Una giornata da 1h su un obiettivo di 2h contribuisce per metà di una giornata piena.

- **30 punti — Qualità della sessione**
  - Durata media delle sessioni degli ultimi 7 giorni rispetto a 45 minuti.

- **20 punti — Streak obiettivo**
  - Giorni consecutivi in cui l'obiettivo giornaliero è stato raggiunto,
    rapportati a 7 giorni.

Il punteggio non misura la qualità intellettuale dello studio né il rendimento
accademico: serve a rendere visibili volume, regolarità e struttura delle sessioni.

## Limiti

- Le fonti sono eterogenee e non riguardano specificamente questa app.
- La formula è trasparente e modificabile, ma non è una scala psicometrica validata.
- Obiettivi troppo ambiziosi possono ridurre il punteggio: è consigliabile scegliere
  un obiettivo realistico e aumentarlo gradualmente.
