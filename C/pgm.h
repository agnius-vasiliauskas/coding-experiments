#pragma once

#include <string.h>
#include <stdio.h>

#define EILUTES_DYDIS_PGM 72
#define SKAICIU_SKIRTUKAI " \t"

#define pakeisti_nepageidaujama_simboli(e,n,r) {char * p = e;\
                    while ((p=strchr(p,n))!=NULL) {*p = r;}}

#define PI 3.14159265359

typedef struct {
    int x;
    int y;
} Koordinate;

typedef struct {
 const unsigned int plotis;
 const unsigned int aukstis;
 unsigned char ** const pikseliai;
} Pgm;

/// daznu funkciju prototipai
void koordDekartoIpoline(float x, float y, float * r, float * a);
void koordPolineIDekarto(float r, float a, float * x, float * y);
int apribotiRezius(int x, int a, int b);
int sulietiDviSpalvas(unsigned char a, unsigned int b, double koeficentas);

/// pgm prototipai
void pgm_nukopijuoti(Pgm * pgmIs, Pgm * pgmI);
void pgm_nuskaityti(Pgm * pgm, char * paveiksliukoByla);
void pgm_irasyti(Pgm * pgm, char * paveiksliukoByla);
void pgm_sukurti(Pgm * pgm, unsigned int plotis, unsigned int aukstis, unsigned char fonas);
void pgm_atlaisvinti(Pgm * pgm);

/// Daznu funkciju kodas

void koordDekartoIpoline(float x, float y, float * r, float * a) {
    *r = sqrt(x*x + y*y);
    *a = atan2(y,x);
}

void koordPolineIDekarto(float r, float a, float * x, float * y) {
    *x = r * cos(a);
    *y = r * sin(a);
}

int apribotiRezius(int x, int a, int b) {
    return x < a ? a : x > b ? b : x;
}

int sulietiDviSpalvas(unsigned char a, unsigned int b, double koeficentas) {
    return b*koeficentas + a*(1. - koeficentas);
}

/// pgm kodas

void pgm_nukopijuoti(Pgm * pgmIs, Pgm * pgmI) {
    if (pgmIs == NULL || pgmI == NULL || pgmIs->aukstis == 0 || pgmIs->plotis == 0) {
        printf("Nenurodytas pirmasis paveiksliukas\n");
        return;
    }
    pgm_sukurti(pgmI, pgmIs->plotis, pgmIs->aukstis, 0);
    int x,y;
    for (x=0; x < pgmIs->plotis; x++)
        for (y=0; y < pgmIs->aukstis; y++)
            pgmI->pikseliai[y][x] = pgmIs->pikseliai[y][x];
}

void pgm_sukurti(Pgm * pgm, unsigned int plotis, unsigned int aukstis, unsigned char fonas) {
    if (pgm == NULL) {
        printf("Nenurodyta struktura !\n");
        return;
    }
    if (pgm->aukstis == 0 || pgm->plotis == 0) {
        printf("Nenurodytas aukstis ar plotis!\n");
        return;
    }
    *(unsigned int*)&pgm->aukstis = aukstis;
    *(unsigned int*)&pgm->plotis = plotis;

    // uzimame dinamini masyva
    int i;
    *(unsigned char ***)&pgm->pikseliai = malloc(sizeof(unsigned char *) * pgm->aukstis);
    for (i=0; i < pgm->aukstis; i++) {
        pgm->pikseliai[i] = malloc(sizeof(unsigned char) * pgm->plotis);
        memset(pgm->pikseliai[i], fonas, sizeof(unsigned char) * pgm->plotis);
    }
}

void pgm_atlaisvinti(Pgm * pgm) {
    if (pgm == NULL || pgm->pikseliai == NULL)
        return;

    if (pgm->aukstis == 0)
        return;

    int i;
    for (i=0; i < pgm->aukstis; i++) {
        free(pgm->pikseliai[i]);
    }
    free(pgm->pikseliai);
    *(unsigned char ***)&pgm->pikseliai = NULL;
    *(unsigned int*)&pgm->aukstis = 0;
    *(unsigned int*)&pgm->plotis = 0;
}

void pgm_irasyti(Pgm * pgm, char * paveiksliukoByla) {
    if (pgm == NULL) {
        printf("Nenurodytas PGM strukturos adresas !\n");
        return;
    }

    if (pgm->aukstis == 0 || pgm->plotis ==0 || pgm->pikseliai == NULL) {
        printf("Neuzpildyta PGM struktura!\n");
        return;
    }

    FILE * fp = fopen(paveiksliukoByla, "w");
    if (fp == NULL) {
        printf("Nepavyko atidaryti bylos rasymui '%s'\n", paveiksliukoByla);
        return;
    }

    fprintf(fp, "%s\n", "P2");
    fprintf(fp, "%s\n", "# Sukurta is C kompiliatoriaus");
    fprintf(fp, "%d %d\n", pgm->plotis, pgm->aukstis);
    fprintf(fp, "%d\n", 255);

    int i,j;
    for (i=0; i < pgm->aukstis; i++) {
        for (j=0; j < pgm->plotis; j++) {
            fprintf(fp, "%d\n", pgm->pikseliai[i][j]);
        }
    }

    fclose(fp);
}

void pgm_nuskaityti(Pgm * pgm, char * paveiksliukoByla) {
    typedef enum {
        PGM_BUSENA_FORMATAS,
        PGM_BUSENA_ISMATAVIMAI,
        PGM_BUSENA_MAX_VERTE,
        PGM_BUSENA_PIKSELIAI
    } PgmBusena;

    if (pgm == NULL) {
        printf("Nenurodytas PGM strukturos adresas !\n");
        return;
    }
    FILE * fp = fopen(paveiksliukoByla, "r");
    if (fp == NULL) {
        printf("Nepavyko nuskaityti bylos '%s'\n", paveiksliukoByla);
        return;
    }

    *(unsigned char ***)&pgm->pikseliai = NULL;
    *(unsigned int*)&pgm->aukstis = 0;
    *(unsigned int*)&pgm->plotis = 0;

    char buf[EILUTES_DYDIS_PGM];
    PgmBusena pgmBusena = PGM_BUSENA_FORMATAS;
    unsigned int maxPikselioVerte;
    unsigned int kelintasPikselis = 0;
    unsigned int i,j;
    unsigned int verte;
    unsigned int pgmEilute = 0;

    while (fgets(buf, EILUTES_DYDIS_PGM, fp) != NULL) {
        pgmEilute++;
        if (strchr(buf, '\n') == NULL && !feof(fp)) {
            printf("Nekorektiskai suformatuota PGM byla '%s' - ties eilute nr: %d\n"
                   "Atsidarykite byla su GIMP ir eksportuokite is naujo !\n", paveiksliukoByla, pgmEilute);
            pgm_atlaisvinti(pgm);
            break;
        }
        pakeisti_nepageidaujama_simboli(buf,'\n','\0');
        pakeisti_nepageidaujama_simboli(buf,'\r','\0');
        if (*buf == '#')
            continue;
        if (pgmBusena == PGM_BUSENA_FORMATAS) {
            if (strcmp(buf, "P2") != 0) {
                printf("Palaikomas tik ASCII PGM formatas !\n");
                break;
            }
            else {
                pgmBusena = PGM_BUSENA_ISMATAVIMAI;
            }
        } else if (pgmBusena == PGM_BUSENA_ISMATAVIMAI) {
                char * skaicius = strtok(buf, SKAICIU_SKIRTUKAI);
                // nuskaitome ploti ir auksti
                while (skaicius != NULL) {
                    if (pgm->plotis == 0)
                        *(unsigned int*)&pgm->plotis = atoi(skaicius);
                    else
                        *(unsigned int*)&pgm->aukstis = atoi(skaicius);
                    skaicius = strtok(NULL, SKAICIU_SKIRTUKAI);
                }
                // uzimame dinamini masyva
                *(unsigned char ***)&pgm->pikseliai = malloc(sizeof(unsigned char *) * pgm->aukstis);
                for (i=0; i < pgm->aukstis; i++) {
                    pgm->pikseliai[i] = malloc(sizeof(unsigned char) * pgm->plotis);
                    memset(pgm->pikseliai[i], 0, sizeof(unsigned char) * pgm->plotis);
                }
                pgmBusena = PGM_BUSENA_MAX_VERTE;
        } else if (pgmBusena == PGM_BUSENA_MAX_VERTE) {
                maxPikselioVerte = atoi(buf);
                pgmBusena = PGM_BUSENA_PIKSELIAI;
        } else if (pgmBusena == PGM_BUSENA_PIKSELIAI) {
                // nustatome visus pikselius masyve
                char * skaicius = strtok(buf, SKAICIU_SKIRTUKAI);
                while (skaicius != NULL) {
                    verte = atoi(skaicius);
                    if (maxPikselioVerte != 255)
                        verte = (unsigned int) (255. * ((double)verte / (double)maxPikselioVerte));
                    i = kelintasPikselis / pgm->plotis;
                    j = kelintasPikselis - i * pgm->plotis;
                    if (i >= pgm->aukstis || j >= pgm->plotis)
                        goto Pabaiga;
                    pgm->pikseliai[i][j] = (unsigned char) verte;
                    kelintasPikselis++;
                    skaicius = strtok(NULL, SKAICIU_SKIRTUKAI);
                }

        }
    }

    Pabaiga:
    fclose(fp);
}
