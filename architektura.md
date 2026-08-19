# 1. Cele aplikacji

Aplikacja jest zaprojektowana do monitorowania stężenia gazu w trakcie jego wydobycia.

Pozwala to użytkownikowi na:

- rejestrację stężenia gazu,
- przeglądanie archiwalnych danych pomiarowych,
- zobaczenie obecnego statusu pomiarowego,
- otrzymywanie alertów w razie niebezpiecznego obecnego stanu,
- przeglądanie procedur bezpieczeństwa,
- analizę danych za pomocą wykresów, map i predykcji,
- zobaczenie stanów archiwalnych dla poszczególnych stacji za pomocą mapy.

Aplikacja skupia się na: 

- stężeniu gazu,
- stworzonym oddzielnie modelu predykcyjnym, 
- analizie danych i jej wizualizacji,
- detekcji anomalii na podstawie klasyfikacji binarnej,
- alertach i procedurach bezpieczeństwa.

Głównym celem aplikacji jest poprawa obecnego monitoringu i wsparcie dla szybszych reakcji na potencjalnie niebezpieczne wycieki gazu.

# 2. Główne ekrany aplikacji

Aplikacja powinna zawierać poniższe, główne ekrany:

## Dashboard

Wyświetla:
- obecne stężenie gazu,
- aktywne alerty,
- wybrane KPI.

## Nowy pomiar

Pozwala na:
- dodanie obecnego stężenia użytkownikowi,
- automatyczne pobranie lokalizacji (integracja GPS),
- zapisanie lokalizacji stacji,
- zapisanie pomiaru.

## Historia pomiarów

Wyświetla:

- listę poprzednich pomiarów, wraz z ich datą,
- po kliknięciu na pomiar pokazuje się stężenie gazu, lokalizacja pomiaru i status binarny tego pomiaru.

Użytkownik powinien mieć opcję na filtrowanie pomiarów na podstawie daty.

## Alerty

Wyświetla:

- aktywne alerty,
- poprzednie alerty wskazujące na niebezpieczeństwo.

## Procedury bezpieczeństwa

Wyświetla instrukcje w razie, gdy występuje stan niebezpieczny. Jeśli po wprowadzeniu pomiaru system zdecyduje, że pomiar jest niebezpieczny, wyświetla alert i od razu przekierowuje do tej zakładki.

## Analiza

Wyświetla:
- wykresy stężenia,
- trendy pomiarów,
- korelacje,
- heatmapy,
- predykcje.

## Ustawienia

Pozwala użytkownikowi na zarządzanie ustawieniami aplikacji, wybrane opcje konfiguracyjne.

# 3. Nawigacja po aplikacji i przepływ użytkownika

Główny ekran nawigacyjny powinien pozwolić użytkownikowi na dostęp do:
- Dashboardu,
- Nowego pomiaru,
- Pomiarów archiwalnych,
- Alertów,
- Analityki,
- Ustawień.

# 5. Specyfikacja Architektury Funkcjonalnej Systemu Monitoringu

Architektura aplikacji została zaprojektowana w modelu wielowarstwowym, ze szczególnym uwzględnieniem separacji logiki biznesowej, akwizycji danych oraz inferencji modeli uczenia maszynowego. System operuje na asynchronicznym przepływie zdarzeń i składa się z czterech głównych modułów.

## Warstwa Akwizycji i Kontekstualizacji Danych (Data Ingestion & Context Layer)

Moduł odpowiedzialny za interfejsy wejściowe oraz wzbogacanie surowych odczytów o metadane przestrzenno - czasowe przed przekazaniem ich do warstwy analitycznej.

- **Interfejs Rejestracji Telemetrii**: Komponent UI zapewniający formularze do manualnego wprowadzania parametrów środowiskowych i procesowych, wyposażony w mechanizmy rygorystycznej walidacji wejścia na poziomie widoku.

- **Serwis Geolokalizacyjny**: Usługa działająca w tle, zintegrowana z interfejsem wprowadzania danych. Automatycznie paruje wprowadzany pomiar z precyzyjnymi współrzędnymi geograficznymi oraz znacznikiem czasu, tworząc kompletny wektor danych przed jego zapisem.

## Silnik Inferencyjny AI i Logika Decyzyjna (ML Inference & Decision Engine)

Rdzeń analityczny systemu, który przetwarza ustandaryzowane dane wejściowe. Platforma AI wbudowana w aplikację ma za zadanie analizować zebrane informacje i automatycznie wykrywać anomalie w stężeniach gazu.

- **Moduł Klasyfikacji Binarnej**: Komponent wykorzystujący zagnieżdżony model ML do analizy napływających odczytów. Proces klasyfikacji odbywa się w czasie rzeczywistym i ma na celu kategoryzację odczytu jako stanu normalnego lub anomalii wycieku.

- **Mechanizm Korelacji Historycznej**: Silnik biznesowy, który ewaluuje bieżące wpisy w kontekście wgranej wcześniej, referencyjnej bazy danych historycznych. Stanowi to punkt odniesienia niezbędny do poprawnego działania predykcji.

- **Dyspozytor Alertów**: Podsystem nasłuchujący wyników z modułu klasyfikacji; w przypadku zwrócenia flagi anomalii, natychmiast inicjuje procedury powiadamiania oraz udostępnia odpowiednie procedury bezpieczeństwa.

## Warstwa Persystencji i Archiwizacji (Persistence & Audit Layer)

Moduł bazodanowy zapewniający trwałość danych lokalnych oraz realizujący funkcje audytowe.

- **Zarządzanie Zbiorem Referencyjnym**: Obsługa zapisu i odczytu wstępnie załadowanych paczek z historycznymi pomiarami dla poszczególnych stacji.

- **Dziennik Audytowy**: Trwały zapis każdego zrealizowanego pomiaru. Rekord bazy obejmuje wprowadzoną wartość, koordynaty przestrzenne, czas operacji oraz przypisaną przez model ML klasyfikację (Anomalia/Norma), co umożliwia późniejszą weryfikację i śledzenie historii zdarzeń.

## Moduł Wizualizacji Przestrzennej i Analityki Stężeń (Spatial Visualization & Dashboarding Layer)

Warstwa prezentacji (UI) odpowiedzialna za agregację przetworzonych danych i ich graficzną reprezentację. Architektura przewiduje zaawansowaną wizualizację danych między innymi poprzez mapy.

- **Pulpit Analityczny Stężeń**: Agregator danych w czasie rzeczywistym, który konsoliduje ostatnie odczyty i prezentuje kluczowe wskaźniki (KPI) dotyczące rozkładu stężeń w porównaniu do danych historycznych.

- **Moduł Analizy Wielowymiarowej**: Komponent renderujący graficzne podsumowania korelacji zjawisk fizycznych, w tym wykresy obrazujące dynamikę stężeń w funkcji odległości oraz warunków atmosferycznych.

- **System Mapowania Gradientowego**: Silnik odpowiedzialny za nakładanie wartości stężeń na podkład mapowy. Umożliwia to generowanie heatmap, które w sposób wizualny przedstawiają zmiany w intensywności zjawiska wraz ze wzrostem odległości od źródła wydobycia. Pozwala to na szybką, wizualną identyfikację obszarów sklasyfikowanych jako anomalie w odniesieniu do rozmieszczenia stacji pomiarowych
