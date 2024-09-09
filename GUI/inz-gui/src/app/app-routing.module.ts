import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

/*Import*/
import {MathjaxModule} from 'mathjax-angular';

const routes: Routes = [];

@NgModule({
  imports: [RouterModule.forRoot(routes), MathjaxModule.forRoot()],
  exports: [RouterModule],
})
export class AppRoutingModule {
}
