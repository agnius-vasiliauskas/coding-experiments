#pragma once

#pragma GCC diagnostic warning "-Wempty-body"

#include <string.h>
#include <stdio.h>
#include <math.h>

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
    float x;
    float y;
} KoordinateF;

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
Koordinate atkarposIrPaveiksliukoKrastoSusikirtimas(Koordinate p1, Koordinate p2, Pgm * pgm);
int atstumasTarpTasku(Koordinate p1, Koordinate p2);
KoordinateF normalizuotiKoordinate(Koordinate koord);
KoordinateF padaugintiKoordinate(KoordinateF koord, float sk);
KoordinateF sudetiKoordinates(KoordinateF koord1, KoordinateF koord2);
KoordinateF atimtiKoordinates(KoordinateF koord1, KoordinateF koord2);

/// pgm prototipai
void pgm_nukopijuoti(Pgm * pgmIs, Pgm * pgmI);
void pgm_nukopijuotiSegmenta(Pgm * pgmIs, Pgm * pgmI, Koordinate blokoPradzia, Koordinate blokoDydis);
void pgm_nuskaityti(Pgm * pgm, char * paveiksliukoByla);
void pgm_irasyti(Pgm * pgm, char * paveiksliukoByla);
void pgm_sukurti(Pgm * pgm, unsigned int plotis, unsigned int aukstis, unsigned char fonas);
void pgm_atlaisvinti(Pgm * pgm);

/// Daznu funkciju kodas

int atstumasTarpTasku(Koordinate p1, Koordinate p2) {
    return sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
}

KoordinateF normalizuotiKoordinate(Koordinate koord) {
    int ilgis = atstumasTarpTasku(koord, (Koordinate){0,0});
    KoordinateF normK = {(float)koord.x/(float)ilgis, (float)koord.y/(float)ilgis};
    return normK;
}

KoordinateF padaugintiKoordinate(KoordinateF koord, float sk) {
    return (KoordinateF){koord.x * sk, koord.y * sk};
}

KoordinateF sudetiKoordinates(KoordinateF koord1, KoordinateF koord2) {
    return (KoordinateF){koord1.x + koord2.x, koord1.y + koord2.y};
}

KoordinateF atimtiKoordinates(KoordinateF koord1, KoordinateF koord2) {
    return (KoordinateF){koord1.x - koord2.x, koord1.y - koord2.y};
}

static Koordinate atkarpuSusikirtimoTaskas(Koordinate p1, Koordinate p2, Koordinate p3, Koordinate p4) {
    Koordinate sus;
    int vardiklis = (p4.y-p3.y)*(p2.x-p1.x)-(p4.x-p3.x)*(p2.y-p1.y);
    float ua = (float)((p4.x-p3.x)*(p1.y-p3.y)-(p4.y-p3.y)*(p1.x-p3.x))/(float)vardiklis;
    float ub = (float)((p2.x-p1.x)*(p1.y-p3.y)-(p2.y-p1.y)*(p1.x-p3.x))/(float)vardiklis;
    if (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0) {
        sus.x = p1.x + ua*(p2.x-p1.x);
        sus.y = p1.y + ua*(p2.y-p1.y);
    }
    else {
        sus = (Koordinate){-1, -1};
    }
    return sus;
}

Koordinate atkarposIrPaveiksliukoKrastoSusikirtimas(Koordinate p1, Koordinate p2, Pgm * pgm) {
    Koordinate susikirtimai[4];
    Koordinate a = (Koordinate){0,0};
    Koordinate b = (Koordinate){pgm->plotis-1,0};
    Koordinate c = (Koordinate){pgm->plotis-1,pgm->aukstis-1};
    Koordinate d = (Koordinate){0,pgm->aukstis-1};

    susikirtimai[0] = atkarpuSusikirtimoTaskas(p1, p2, a, b);
    susikirtimai[1] = atkarpuSusikirtimoTaskas(p1, p2, b, c);
    susikirtimai[2] = atkarpuSusikirtimoTaskas(p1, p2, a, d);
    susikirtimai[3] = atkarpuSusikirtimoTaskas(p1, p2, d, c);
    int i;
    for (i=0; i < 4; i++)
        if (susikirtimai[i].x != -1 && susikirtimai[i].y != -1)
            return susikirtimai[i];
    return (Koordinate){-1,-1};
}

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

void pgm_nukopijuotiSegmenta(Pgm * pgmIs, Pgm * pgmI, Koordinate blokoPradzia, Koordinate blokoDydis) {
    if (pgmIs == NULL || pgmI == NULL || pgmIs->aukstis == 0 || pgmIs->plotis == 0) {
        printf("Nenurodytas pirmasis paveiksliukas\n");
        return;
    }
    pgm_sukurti(pgmI, blokoDydis.x, blokoDydis.y, 0);
    int x,y;
    for (x=0; x < blokoDydis.x; x++)
        for (y=0; y < blokoDydis.y; y++)
            pgmI->pikseliai[y][x] = pgmIs->pikseliai[y+blokoPradzia.y][x+blokoPradzia.x];
}

void pgm_sukurti(Pgm * pgm, unsigned int plotis, unsigned int aukstis, unsigned char fonas) {
    if (pgm == NULL) {
        printf("Nenurodyta struktura !\n");
        return;
    }
    if (aukstis == 0 || plotis == 0) {
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
