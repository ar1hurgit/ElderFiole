# ElderFiole

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spigot](https://img.shields.io/badge/Spigot-E57E25?style=for-the-badge&logo=minecraft&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)

![GitHub last commit](https://img.shields.io/github/last-commit/ar1hurgit/ElderFiole?style=flat-square)
![GitHub top language](https://img.shields.io/github/languages/top/ar1hurgit/ElderFiole?style=flat-square)
![GitHub language count](https://img.shields.io/github/languages/count/ar1hurgit/ElderFiole?style=flat-square)
![GitHub repo size](https://img.shields.io/github/repo-size/ar1hurgit/ElderFiole?style=flat-square)

**ElderFiole** est un plugin Minecraft Spigot qui ajoute des fioles d'XP métier pour le plugin **Jobs Reborn**. Il permet aux joueurs de recevoir des boosts temporaires d'XP pour des métiers spécifiques via des fioles consommables.

---

## 🌟 Fonctionnalités

*   🧪 **Fioles d'XP Métier** : Donnez des items spéciaux qui, une fois consommés, appliquent un multiplicateur d'XP temporaire.
*   📅 **Cooldown Quotidien** : Système de récupération quotidienne intégré (optionnel).
*   🎨 **Personnalisation Complète** :
    *   Nom et Lore des fioles configurables.
    *   Support du **CustomModelData** pour les resource packs.
    *   Effet de lueur (Glow) désactivable.
*   ⚡ **Commandes Admin** : Commandes simples pour distribuer les fioles.
*   ⏱️ **Gestion du Temps** : Les joueurs peuvent voir le temps restant de leurs boosts actifs.

## 🛠️ Commandes & Permissions

| Commande | Permission | Description |
| :--- | :--- | :--- |
| `/fiole give <multiplicateur> <job> <durée> <joueur> <quantité>` | `elderfiole.fiole.give` | Donner des fioles à un joueur. |
| `/fiole timeleft` | `elderfiole.fiole.timeleft` | Voir le temps restant des boosts actifs. |
| `/fiole reload` | `elderfiole.reload` | Recharger la configuration. |
| `/dailyfiole` | `elderfiole.dailyfiole` | Récupérer une fiole aléatoire (1x par jour). |

### Exemples
*   Donner un boost x2 pendant 30 minutes pour le métier Miner :
    *   `/fiole give 2.0 Miner 30 PseudoDuJoueur 1`

## ⚙️ Configuration

Le fichier `config.yml` permet de tout ajuster. Voici un extrait des options principales :

```yaml
daily-vial:
  multiplier: 1.10
  duration: 120 # En minutes
  cooldown: 24 # En heures

item:
  glow: true
  custom-model-data: 6235523 # ID pour texture custom

messages:
  vial-name: "&b&lFiole d'XP Métier"
  active-boosts-header: "&eVos boosts actifs:"
```

## 📦 Installation

1.  Téléchargez le plugin (non disponible publiquement pour l'instant).
2.  Placez le `.jar` dans le dossier `plugins/` de votre serveur.
3.  Assurez-vous d'avoir **Jobs Reborn** installé.
4.  Redémarrez votre serveur.

---
*Développé avec ❤️ pour ElderFiole.*
