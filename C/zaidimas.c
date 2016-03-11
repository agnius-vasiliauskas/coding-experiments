#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <SDL.h>
#include "pgm.h"

typedef struct {
    Uint32 backGroundColor;
    SDL_Window * window;
    SDL_Surface * screen;
    SDL_Rect imgRect;
    SDL_Surface * imgSurf;
} Game;

void initGame(Game * game) {
    // initialize SDL video
    if ( SDL_Init( SDL_INIT_VIDEO ) < 0 )
    {
        printf( "Unable to init SDL: %s\n", SDL_GetError() );
        exit(0);
    }

    // make sure SDL cleans up before exit
    atexit(SDL_Quit);

    // create a new window
    game->window = SDL_CreateWindow("Zaidimas",SDL_WINDOWPOS_CENTERED,SDL_WINDOWPOS_CENTERED,640,480, SDL_WINDOW_OPENGL);
    game->screen = SDL_GetWindowSurface(game->window);

    if ( !game->window )
    {
        printf("Unable to set 640x480 window: %s\n", SDL_GetError());
        exit(0);
    }

    if (!game->screen) {
        printf("Unable to get window surface!\n");
        exit(0);
    }

    // load an image
    game->imgSurf = SDL_LoadBMP("sheep.bmp");

    if (!game->imgSurf)
    {
        printf("Unable to load bitmap: %s\n", SDL_GetError());
        exit(0);
    }

    int transparentKlaida = SDL_SetColorKey(game->imgSurf, SDL_TRUE, SDL_MapRGB(game->imgSurf->format, 127, 127, 127) );
    if (transparentKlaida) {
        printf("problema (%d) nustatant transparency\n", transparentKlaida);
        exit(0);
    }

    game->imgRect = (SDL_Rect){0};
    game->imgRect.x = (game->screen->w - game->imgSurf->w)/2;
    game->imgRect.y = (game->screen->h - game->imgSurf->h)/2;

    game->backGroundColor = SDL_MapRGB(game->screen->format, 0, 200, 100);
}

void updateEvents(Game * game) {
    SDL_Event event;
    while (SDL_PollEvent(&event))
    {
        if (event.type == SDL_QUIT || (event.type == SDL_KEYDOWN && event.key.keysym.sym == SDLK_ESCAPE )) {
            SDL_FreeSurface(game->imgSurf);
            SDL_FreeSurface(game->screen);
            SDL_DestroyWindow(game->window);
            SDL_Quit();
            exit(0);
        }
    }
}

Uint32 updateGame(Uint32 interval, void * param)
{
    Game * game = (Game*)param;

    static int seedInicializuotas;
    if (!seedInicializuotas)
        seedInicializuotas = (srand(time(NULL)), 1);

    int arUzRibu(Koordinate koord) {
        return koord.x < 0 ||
               koord.y < 0 ||
               koord.x > game->screen->w - game->imgSurf->w ||
               koord.y > game->screen->h - game->imgSurf->h;
    }

    static Koordinate kryptis;
    static int kelintasFreimas;
    kelintasFreimas++;

    if ((kryptis.x == 0 && kryptis.y == 0) || kelintasFreimas % 200 == 0)
        kryptis = (Koordinate){50-rand()%101, 50-rand()%101};

    int kryptiesIlgis = atstumasTarpTasku(kryptis, (Koordinate){0,0});
    float xVienetinis = (float)kryptis.x / (float)kryptiesIlgis;
    float yVienetinis = (float)kryptis.y / (float)kryptiesIlgis;
    Koordinate nauja = {game->imgRect.x + 2. * xVienetinis, game->imgRect.y + 2. * yVienetinis};

    if (arUzRibu(nauja))
        kryptis = (Koordinate){-kryptis.x, -kryptis.y};
    else {
        game->imgRect.x = nauja.x;
        game->imgRect.y = nauja.y;
    }

    return interval;
}

void drawGame(Game * game) {
        SDL_FillRect(game->screen, 0, game->backGroundColor);

        SDL_BlitSurface(game->imgSurf, NULL, game->screen, &game->imgRect);

        SDL_UpdateWindowSurface(game->window);
}

int main (void)
{
    Game game;
    initGame(&game);
    SDL_AddTimer(10, updateGame, &game);

    while (1)
    {
        updateEvents(&game);
        drawGame(&game);
    }

    return 0;
}
