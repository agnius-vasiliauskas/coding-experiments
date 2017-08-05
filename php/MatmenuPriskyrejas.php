<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.48
 */

class MatmenuPriskyrejas {
    public $rysiai = [];

    function __construct($isparsintiSakiniai)
    {
        foreach ($isparsintiSakiniai as $sakinys) {
            if (count($sakinys->arrObjektai) == 0)
                continue;

            $objRysiai = null;

            foreach ($sakinys->arrMatmenys as $matmuo) {
                // susigeneruojame objekta kam priskirsim matmenis
                if (count($sakinys->arrObjektai) == 1) {
                    $objRef = $sakinys->arrObjektai[0];
                    if ($objRysiai === null) {
                        $objRysiai = new Objektas();
                        $objRysiai->id = $objRef->id;
                        $objRysiai->pavadinimas = $objRef->pavadinimas;
                    }
                }
                else
                    throw new Exception("Nemoku išspręsti uždavinio");

                $objRysiai->arrMatmenys[] = $matmuo;
            }

            if ($objRysiai) {
                $this->rysiai[] = $objRysiai;
            }
        }
    }
}
